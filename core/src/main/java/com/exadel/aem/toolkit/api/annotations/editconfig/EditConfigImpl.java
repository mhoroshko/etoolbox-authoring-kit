package com.exadel.aem.toolkit.api.annotations.editconfig;

import com.exadel.aem.toolkit.api.annotations.editconfig.listener.Listener;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class EditConfigImpl implements EditConfig {

    private final EditConfig config;
    private final InplaceEditingConfigImpl[] configs;

    public EditConfigImpl(EditConfig config) {
        this.config = config;
        this.configs = Arrays.stream(config.inplaceEditing()).map(InplaceEditingConfigImpl::new).toArray(value -> new InplaceEditingConfigImpl[value]);

    }

    @Override
    public String[] actions() {
        return config.actions();
    }
    @Override
    public String emptyText() {
        return config.emptyText();
    }
    @Override
    public boolean inherit() {
        return config.inherit();
    }
    @Override
    public EditConfigLayout dialogLayout() {
        return config.dialogLayout();
    }
    @Override
    public DropTargetConfig[] dropTargets() {
        return config.dropTargets();
    }
    @Override
    public FormParameter[] formParameters() {
        return config.formParameters();
    }
    @Override
    public InplaceEditingConfig[] inplaceEditing() {
        return configs;
    }
    @Override
    public Listener[] listeners() {
        return config.listeners();
    }
    @Override
    public Class<? extends Annotation> annotationType() {
        return config.annotationType();
    }
}
