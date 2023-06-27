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
package com.exadel.aem.toolkit.plugin.handlers.widgets.common;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define {@code granite:data} and similar attributes for the current Granite component
 */
public class AttributeAnnotationHandler implements BiConsumer<Source, Target> {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code
     * Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        Attribute attributeAnnotation = source.adaptTo(Attribute.class);
        Data[] standaloneDataEntries = source.adaptTo(Data[].class);
        List<Data> dataEntries = Stream.concat(
                attributeAnnotation != null ? Arrays.stream(attributeAnnotation.data()) : Stream.empty(),
                Arrays.stream(standaloneDataEntries))
            .filter(Data::persist)
            .collect(Collectors.toList());

        if (dataEntries.isEmpty()) {
            return;
        }
        Target graniteDataElement = target.getOrCreateTarget(CoreConstants.NN_GRANITE_DATA);
        dataEntries.forEach(entry -> graniteDataElement.attribute(entry.name(), entry.value()));
    }
}
