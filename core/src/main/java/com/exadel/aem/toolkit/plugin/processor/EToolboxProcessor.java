package com.exadel.aem.toolkit.plugin.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;

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

        Set<? extends Element> components = roundEnv.getElementsAnnotatedWith(AemComponent.class);

        for (Element component : components) {
            ClassInfo classInfo = new ClassInfo(component.toString());
            populateAnnotations(component, classInfo);
            populateMembers(component, classInfo);
            try {
                //System.getProperty("java.io.temp.dir");
                FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "etoolbox", component.getSimpleName() + ".json");
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.disableHtmlEscaping().create();
                Writer writer = fileObject.openWriter();
                writer.write(gson.toJson(classInfo));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void populateMembers(Element element, ClassInfo classInfo) {
        for (Element member : element.getEnclosedElements()) {
            MemberInfo memberInfo = new MemberInfo(member.getSimpleName().toString());
            populateAnnotations(member, memberInfo);
            classInfo.addMemberInfo(memberInfo);
        }
    }

    private void populateAnnotations(Element member, BaseInfo baseInfo) {
        for (AnnotationMirror annotationMirror : member.getAnnotationMirrors()) {
            baseInfo.addAnnotationInfo(new AnnotationInfo(annotationMirror));
        }
    }
}
