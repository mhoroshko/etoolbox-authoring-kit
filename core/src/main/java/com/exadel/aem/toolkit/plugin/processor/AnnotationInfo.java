package com.exadel.aem.toolkit.plugin.processor;

import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class AnnotationInfo {

    private final String name;

    private final Map<String, Object> values;

    public AnnotationInfo(AnnotationMirror annotationMirror) {
        this.name = annotationMirror.getAnnotationType().toString();
        this.values = new LinkedHashMap<>();
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> elementValue : elementValues.entrySet()) {
            TypeMirror returnType = elementValue.getKey().getReturnType();
            String key = StringUtils.remove(elementValue.getKey().toString(), "()");
            Object value = elementValue.getValue().getValue();
            if (TypeKind.ARRAY.equals(returnType.getKind())) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty() && list.get(0) instanceof AnnotationMirror) {
                    this.values.put(key, list.stream().map(annotation -> new AnnotationInfo(((AnnotationMirror) annotation))).toArray());
                } else {
                    this.values.put(key, list);
                }
            } else {
                if (value instanceof AnnotationMirror) {
                    this.values.put(key, new AnnotationInfo((AnnotationMirror) value));
                } else {
                    this.values.put(key, value.toString());
                }
            }
        }
    }

    public Map<String, Object> getValues() {
        return this.values;
    }

    public String getName() {
        return this.name;
    }
}
