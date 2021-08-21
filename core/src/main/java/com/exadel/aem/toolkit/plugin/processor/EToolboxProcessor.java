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
    private Gson gson;
    private FileObject properties;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
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
            processElement(component);
            // TODO: Process classes from AemComponent#views();
        }
        return true;
    }

    private void processElement(Element element) {
        ClassInfo classInfo = new ClassInfo(element.toString());
        populateAnnotations(element, classInfo);
        populateMembers(element, classInfo);
        try {
            // TODO: Move all data to temp folder
            //System.getProperty("java.io.temp.dir");
            FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "etoolbox", element + ".json");
            Writer writer = fileObject.openWriter();
            writer.write(gson.toJson(classInfo));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: Move this logic to Info Classes
    private void populateMembers(Element element, ClassInfo classInfo) {
        for (Element member : element.getEnclosedElements()) {
            MemberInfo memberInfo = new MemberInfo(member.getSimpleName().toString());
            populateAnnotations(member, memberInfo);
            classInfo.addMemberInfo(memberInfo);
        }
    }

    // TODO: Move this logic to Info Classes
    private void populateAnnotations(Element member, BaseInfo baseInfo) {
        for (AnnotationMirror annotationMirror : member.getAnnotationMirrors()) {
            baseInfo.addAnnotationInfo(new AnnotationInfo(annotationMirror));
        }
    }
}
