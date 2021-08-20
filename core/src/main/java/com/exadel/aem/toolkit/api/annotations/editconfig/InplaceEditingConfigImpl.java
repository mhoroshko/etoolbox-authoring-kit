package com.exadel.aem.toolkit.api.annotations.editconfig;

import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.ExtendsWrapper;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;

import java.lang.annotation.Annotation;

public class InplaceEditingConfigImpl implements InplaceEditingConfig {

    private final InplaceEditingConfig config;

    public InplaceEditingConfigImpl(InplaceEditingConfig config) {
        this.config = config;
    }

    @Override
    public String type() {
        return config.type();
    }
    @Override
    public String editElementQuery() {
        return config.editElementQuery();
    }
    @Override
    public String name() {
        return config.name();
    }
    @Override
    public String title() {
        return config.title();
    }
    @Override
    public String propertyName() {
        return config.propertyName();
    }
    @Override
    public String textPropertyName() {
        return config.textPropertyName();
    }
    @Override
    public Extends richText() {
        return new ExtendsWrapper(config.richText());
    }
    @Override
    public RichTextEditor richTextConfig() {
        return config.richTextConfig();
    }
    @Override
    public Class<? extends Annotation> annotationType() {
        return config.annotationType();
    }
}
