package com.exadel.aem.toolkit.plugin.processor;

import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.ExtendsWrapper;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.HandlerChains;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.targets.Targets;

import static com.exadel.aem.toolkit.plugin.utils.DialogConstants.NN_ROOT;

//@SupportedAnnotationTypes("com.exadel.aem.toolkit.api.annotations.main.AemComponent")
@SupportedAnnotationTypes("*")
public class EToolboxProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;
    private Types typeUtils;
    private FileObject properties;


    private static final String LOCATION = "C:\\Users\\user\\IdeaProjects\\etoolbox-authoring-kit\\samples\\ui.apps\\src\\main\\content\\jcr_root\\apps\\etoolbox-authoring-kit\\samples\\components\\content\\abilities-component";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        try {
            this.properties = filer.getResource(StandardLocation.CLASS_OUTPUT, "", "etoolbox.properties");
        } catch (IOException e) {
            // ignored
        }

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> components = roundEnv.getElementsAnnotatedWith(EditConfig.class);
        int i = 0;

        Target[] targets = new Target[components.size()];

        for (Element component : components) {
            Target target = Targets.newInstance(NN_ROOT, Scopes.CQ_EDIT_CONFIG);
            HandlerChains.forScope(Scopes.CQ_EDIT_CONFIG).accept(Sources.fromElement(component), target);
            targets[i++] = target;
            Sources.fromElement(component).adaptTo(Extends.class);

        }

        i = 10;
        return true;
    }

    private Extends getExtends(Extends extend) {
        return new ExtendsWrapper(extend);
    }

}
