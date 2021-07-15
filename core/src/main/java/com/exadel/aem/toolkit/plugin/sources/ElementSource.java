package com.exadel.aem.toolkit.plugin.sources;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;

import org.checkerframework.javacutil.AnnotationUtils;

public class ElementSource extends SourceImpl {

    private final Element value;

    public ElementSource(Element value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return value.getSimpleName().toString();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    Annotation[] getDeclaredAnnotations() {
        List<Class<?>> annotationMirrors = value.getAnnotationMirrors().stream().map(AnnotationUtils::annotationMirrorToClass).collect(Collectors.toList());
        Annotation[] result = new Annotation[annotationMirrors.size()];
        int i = 0;
        for (Class annotation : annotationMirrors) {
            result[i++] = value.getAnnotation(annotation);
        }
        return result;
    }
    @Override
    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return value.getAnnotationsByType(annotationClass);
    }
    @Override
    <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return value.getAnnotation(annotationClass);
    }
}
