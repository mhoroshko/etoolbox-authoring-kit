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

package com.exadel.aem.toolkit.core.handlers.assets.dependson;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnConfig;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnParam;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRefTypes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * {@code BiConsumer<Source, Target>} implementation used to create markup responsible for AEM Authoring Toolkit {@code DependsOn} functionality
 */
public class DependsOnHandler implements BiConsumer<Source, Target> {

    static final String EMPTY_VALUES_EXCEPTION_MESSAGE = "Non-empty string values required for DependsOn params";

    private static final String TERM_SEPARATOR = "-";

    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        if (source.adaptTo(DependsOn.class) != null) {
            handleDependsOn(source.adaptTo(DependsOn.class), target);
        } else if (source.adaptTo(DependsOnConfig.class) != null) {
            handleDependsOnConfig(source.adaptTo(DependsOnConfig.class), target);
        }
        handleDependsOnRefValue(source, target);
    }

    /**
     * Called by {@link DependsOnHandler#accept(Source, Target)} to store particular {@code DependsOn} value in {@link Target}
     * @param value Current {@link DependsOn} value
     * @param target Current {@link Target} instance
     */
    private void handleDependsOn(DependsOn value, Target target) {
        if (StringUtils.isAnyBlank(value.query(), value.action())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }
        Map<String, String> valueMap = Maps.newHashMap();
        String escapedQuery = StringUtils.replace(value.query(), ";", "\\\\;");
        valueMap.put(DialogConstants.PN_DEPENDS_ON, escapedQuery);
        valueMap.put(DialogConstants.PN_DEPENDS_ON_ACTION, value.action());
        valueMap.putAll(buildParamsMap(value, 0));
        PluginXmlUtility.appendDataAttributes(target, valueMap);
    }

    /**
     * Called by {@link DependsOnHandler#accept(Source, Target)} to store {@code DependsOnConfig} value in {@link Target}
     * @param value Current {@link DependsOnConfig} value
     * @param target Current {@link Target} instance
     */
    private void handleDependsOnConfig(DependsOnConfig value, Target target) {
        List<DependsOn> validDeclarations = Arrays.stream(value.value())
                .filter(dependsOn -> StringUtils.isNoneBlank(dependsOn.action(), dependsOn.query()))
                .collect(Collectors.toList());

        if (value.value().length != validDeclarations.size()) {
            PluginRuntime.context().getExceptionHandler()
                    .handle(new ValidationException(EMPTY_VALUES_EXCEPTION_MESSAGE));
        }

        Map<String, String> valueMap = new HashMap<>();

        String queries = validDeclarations.stream()
                .map(DependsOn::query)
                .map(str -> StringUtils.replace(str, ";", "\\\\;"))
                .collect(Collectors.joining(DialogConstants.VALUE_SEPARATOR));
        String actions = validDeclarations.stream()
                .map(DependsOn::action)
                .collect(Collectors.joining(DialogConstants.VALUE_SEPARATOR));

        valueMap.put(DialogConstants.PN_DEPENDS_ON, queries);
        valueMap.put(DialogConstants.PN_DEPENDS_ON_ACTION, actions);

        Map<String, Integer> counter = new HashMap<>();
        validDeclarations.stream()
                // Counting actions separately
                .map(dependsOn -> DependsOnHandler.buildParamsMap(dependsOn, counter.merge(dependsOn.action(), 1, Integer::sum) - 1))
                .forEach(valueMap::putAll);

        PluginXmlUtility.appendDataAttributes(target, valueMap);
    }

    /**
     * Build {@code DependsOnParam} parameters for the passed {@code DependsOn} annotation
     * Param pattern:
     * - for the first action (index = 0): dependson-{action}-{param}
     * - otherwise: dependson-{action}-{param}-{index}
     *
     * @param dependsOn current {@link DependsOn} value
     * @param index index of action
     */
    private static Map<String, String> buildParamsMap(DependsOn dependsOn, int index){
        Map<String, String> valueMap = new HashMap<>();
        for (DependsOnParam param : dependsOn.params()) {
            String paramName =
                    StringUtils.joinWith(TERM_SEPARATOR, DialogConstants.PN_DEPENDS_ON, dependsOn.action(), param.name());
            if (index > 0) {
                paramName = StringUtils.joinWith(TERM_SEPARATOR, paramName, index);
            }
            valueMap.put(paramName, param.value());
        }
        return valueMap;
    }

    /**
     * Called by {@link DependsOnHandler#accept(Source, Target)} to store particular {@code DependsOnRef} value in {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    private void handleDependsOnRefValue(Source source, Target target) {
        DependsOnRef value = source.adaptTo(DependsOnRef.class);
        if (value == null) {
            return;
        }

        String dependsOnRefName = value.name();
        if (StringUtils.isBlank(dependsOnRefName)) {
            dependsOnRefName = source.getName();
        }

        Map<String, String> valueMap = Maps.newHashMap();
        valueMap.put(DialogConstants.PN_DEPENDS_ON_REF, dependsOnRefName);
        if (!value.type().toString().equals(DependsOnRefTypes.AUTO.toString())) {
            valueMap.put(DialogConstants.PN_DEPENDS_ON_REFTYPE, value.type().toString().toLowerCase());
        }
        if (value.lazy()) {
            valueMap.put(DialogConstants.PN_DEPENDS_ON_REFLAZY, StringUtils.EMPTY);
        }
        PluginXmlUtility.appendDataAttributes(target, valueMap);
    }
}
