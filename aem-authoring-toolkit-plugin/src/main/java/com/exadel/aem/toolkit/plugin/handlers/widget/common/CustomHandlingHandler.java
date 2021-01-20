/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.plugin.handlers.widget.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.PluginReflectionUtility;

/**
 * Stores properties coming from custom annotations and, optionally, processed by custom handlers
 * to a Granite UI target
 */
public class CustomHandlingHandler implements BiConsumer<Source, Target> {

    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        List<Class<? extends Annotation>> sourceAnnotations = PluginReflectionUtility.getSourceAnnotations(source);
        List<DialogWidgetHandler> handlers;

        // Modern handlers mapping approach -- via @Handles annotation
        handlers = new ArrayList<>(PluginRuntime.context().getReflectionUtility().getCustomDialogWidgetHandlers(sourceAnnotations));

        // Legacy handlers mapping approach -- via source<->name mapping
        List<DialogWidgetHandler> sourceToNameMappingHandlers = PluginReflectionUtility.getSourceAnnotations(source)
            .stream()
            .filter(a -> a.isAnnotationPresent(DialogWidgetAnnotation.class))
            .map(a -> a.getAnnotation(DialogWidgetAnnotation.class).source())
            .flatMap(CustomHandlingHandler::getMatchedHandlersByName)
            .collect(Collectors.toList());
        handlers.addAll(sourceToNameMappingHandlers);

        // Extract legacy handlers that accept(Source, Target)
        List<DialogWidgetHandler> legacyHandlers = handlers.stream().filter(CustomHandlingHandler::isLegacyHandler).collect(Collectors.toList());
        if (!legacyHandlers.isEmpty()) {
            Field field = source.adaptTo(Field.class);
            Element element = getElement(target);
            if (element != null) {
                // This assignment is for legacy dialog widget handlers, will not interfere with modern handlers
                PluginRuntime.context().getXmlUtility().setDocument(element.getOwnerDocument());
                legacyHandlers.forEach(handler -> handler.accept(element, field));
                target.mapProperties(element);
            }
        }

        // Process modern handlers that accept(Element, Field)
        Collection<DialogWidgetHandler> modernHandlers = CollectionUtils.subtract(handlers, legacyHandlers);
        modernHandlers.forEach(handler -> handler.accept(source, target));
    }

    private static Stream<DialogWidgetHandler> getMatchedHandlersByName(String source) {
        if (StringUtils.isEmpty(source)) {
            return Stream.empty();
        }
        return PluginRuntime.context()
            .getReflectionUtility()
            .getCustomDialogWidgetHandlers()
            .stream()
            .filter(handler -> StringUtils.equals(source, handler.getName()));
    }

    private static boolean isLegacyHandler(DialogWidgetHandler handler) {
        try {
            Method legacyAcceptMethod = handler.getClass().getMethod("accept", Element.class, Field.class);
            return !legacyAcceptMethod.isDefault(); // if it's not default, it has been overridden which is true for actual handlers
        } catch (NoSuchMethodException | SecurityException e) {
            // This is a valid situation, no particular processing needed
            return false;
        }
    }

    private static Element getElement(Target target) {
        try {
            return target.adaptTo(Document.class).getDocumentElement();
        } catch (Exception e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
        return null;
    }

}
