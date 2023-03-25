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
package com.exadel.aem.toolkit.core.injectors.models.requestattribute;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CalendarCollections extends ModelBase<Collection<Calendar>>{

    @RequestAttribute
    private Set<Cloneable> value;

    @Self
    private Supplier supplier;

    @Inject
    public CalendarCollections(@RequestAttribute @Named(ATTRIBUTE_VALUE) List<Calendar> value) {
        super(value);
    }

    public Collection<Calendar> getValue() {
        return value.stream().map(Calendar.class::cast).collect(Collectors.toList());
    }

    @Override
    public ValueSupplier<Collection<Calendar>> getValueSupplier() {
        return supplier;
    }

    @Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public interface Supplier extends ValueSupplier<Collection<Calendar>> {
        @RequestAttribute(name = ATTRIBUTE_VALUE)
        @Override
        List<Calendar> getValue();
    }
}
