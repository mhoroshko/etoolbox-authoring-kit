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
package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.wrappers.DeepReadValueMapDecorator;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.exadel.aem.toolkit.api.annotations.injectors.EnumValue;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.CastUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

/**
 * Provides injecting into a Sling model values of an {@link Enum}
 * @see EnumValue
 * @see BaseInjector
 */
@Component(
    service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING)
public class EnumValueInjector extends BaseInjector<EnumValue> {

    public static final String NAME = "eak-etoolbox-enum-injector";

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    EnumValue getManagedAnnotation(AnnotatedElement element) {
        return element.getAnnotation(EnumValue.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object getValue(Object adaptable, String name, Type type, EnumValue annotation) {

        Class<?> componentType = null;
        if (TypeUtil.isSupportedCollectionOrArrayOfType(type, Class::isEnum, true)) {
            componentType = TypeUtil.getElementType(type);
        } else if (type instanceof Class && ((Class<?>) type).isEnum()) {
            componentType = (Class<?>) type;
        }
        if (componentType == null) {
            return null;
        }

        DeepReadValueMapDecorator valueMap = new DeepReadValueMapDecorator(
            AdaptationUtil.getResource(adaptable),
            AdaptationUtil.getValueMap(adaptable));
        String effectiveName = StringUtils.defaultIfEmpty(annotation.name(), name);

        Object valueMapValue = valueMap.get(effectiveName);
        if (valueMapValue == null) {
            return null;
        }
        BiFunction<Object, Type, Object> converter = annotation.valueMember().isEmpty()
            ? EnumValueInjector::getEnumValue
            : (value, t) -> getEnumValue(value, t, annotation.valueMember());

        return CastUtil.toType(valueMapValue, type, converter);
    }

    /**
     * Attempts to retrieve an enum constant based on the provided value
     * @param value The value that identifies the enum constant
     * @param type  The type of the enum
     * @return A valid enum constant if there is one matching the given value. Otherwise, {@code null}
     */
    @SuppressWarnings("unchecked")
    private static Object getEnumValue(Object value, Type type) {
        Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type;
        return Arrays.stream(enumType.getEnumConstants())
            .filter(constant -> StringUtils.equalsAnyIgnoreCase(value.toString(), constant.name(), constant.toString()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Attempts to retrieve an enum constant based on the provided value and the name of a method/field this value
     * should originate from
     * @param value      The value that identifies the enum constant
     * @param type       The type of the enum
     * @param memberName The name of the constant's field or method that is queried to compare with the given value
     * @return A valid enum constant if there is one matching the given value. Otherwise, {@code null}
     */
    @SuppressWarnings("unchecked")
    private static Object getEnumValue(Object value, Type type, String memberName) {
        Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type;
        return Arrays.stream(enumType.getEnumConstants())
            .filter(constant -> isMatch(constant, memberName, value.toString()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns whether the given enum constant corresponds to the provided value because it is the value of the
     * specified constant's method/field
     * @param enumConstant An enum that we test for a correspondence
     * @param memberName   The name of the constant's field or method that is queried to compare with the given value
     * @param value        The value used for the comparison
     * @return True or false
     */
    private static boolean isMatch(Object enumConstant, String memberName, String value) {
        String invocationResult = invokeMethodSilently(enumConstant, memberName);
        if (invocationResult == null) {
            invocationResult = getFieldValueSilently(enumConstant, memberName);
        }
        return StringUtils.defaultString(invocationResult).equals(value);
    }

    /**
     * Retrieves the value of a method from an enum constant object without throwing ex exception
     * @param value The enum constant whose method invocation result is being retrieved
     * @param name  The name of the method that we want to invoke
     * @return A string value if was able to find the requested method and invoke it; otherwise, {@code null}
     */
    private static String invokeMethodSilently(Object value, String name) {
        try {
            Method method = value.getClass().getMethod(name);
            Object result = method.invoke(value);
            return result != null ? result.toString() : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * Retrieves the value of a field from an enum constant object without throwing ex exception
     * @param value The enum constant whose field value is being retrieved
     * @param name  The name of the field that we want to retrieve
     * @return A string value if was able to find the requested field and query for its value; otherwise, {@code null}
     */
    private static String getFieldValueSilently(Object value, String name) {
        try {
            Field field = value.getClass().getField(name);
            Object result = field.get(value);
            return result != null ? result.toString() : null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
