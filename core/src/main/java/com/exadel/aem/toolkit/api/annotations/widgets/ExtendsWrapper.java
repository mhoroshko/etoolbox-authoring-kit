package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.Annotation;
import javax.lang.model.type.MirroredTypeException;

public class ExtendsWrapper implements Extends {

    private final Extends value;

    public ExtendsWrapper(Extends value) {
        this.value = value;
    }

    @Override
    public Class<?> value() {
        try {
            return value.value();
        } catch (MirroredTypeException e) {
            try {
                return Class.forName(e.getTypeMirror().toString());
            } catch (ClassNotFoundException classNotFoundException) {
                return null;
            }
        }
    }
    @Override
    public String field() {
        return value.field();
    }
    @Override
    public Class<? extends Annotation> annotationType() {
        return value.annotationType();
    }
}
