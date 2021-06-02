package com.exadel.aem.toolkit.core.processor;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.util.Map;

public class ExtendsMetaData {

    private Map<? extends ExecutableElement, ? extends AnnotationValue> originAnnotation;

    private Map<? extends ExecutableElement, ? extends AnnotationValue> overrideAnnotation;

    public void setOriginAnnotation(Map<? extends ExecutableElement, ? extends AnnotationValue> originAnnotation) {
        this.originAnnotation = originAnnotation;
    }

    public void setOverrideAnnotation(Map<? extends ExecutableElement, ? extends AnnotationValue> overrideAnnotation) {
        this.overrideAnnotation = overrideAnnotation;
    }
}
