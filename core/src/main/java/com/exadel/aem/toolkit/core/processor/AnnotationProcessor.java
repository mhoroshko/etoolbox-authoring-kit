package com.exadel.aem.toolkit.core.processor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;

import com.google.gson.Gson;

import com.exadel.aem.toolkit.api.annotations.widgets.Extends;

@SupportedAnnotationTypes("com.exadel.aem.toolkit.api.annotations.widgets.Extends")
public class AnnotationProcessor extends AbstractProcessor {

    private static final String PACKAGE_NAME = "com.exadel.aem.toolkit.api.annotations.widgets.";
    private static final String DIALOG_FIELD = "DialogField";
    private static final String EXTENDS = "Extends";

    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!INIT!!!!!!!!!!!!!!");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        return true;
    }

    private void changeExtends(Element element) {
        Extends annotation = element.getAnnotation(Extends.class);
        TypeElement classTypeElement;
        try {
            Class<?> clazz = annotation.value();
            classTypeElement = elementUtils.getTypeElement(clazz.getCanonicalName());
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            classTypeElement = (TypeElement) classTypeMirror.asElement();
        }

        Element targetElement = getElementFromClassType(classTypeElement, annotation.field());
        ExtendsMetaData extendsMetaData = new ExtendsMetaData();
        extendsMetaData.setOriginAnnotation(getAnnotationMirror(targetElement).getElementValues());
        extendsMetaData.setOverrideAnnotation(getAnnotationMirror(element).getElementValues());
        writeData(extendsMetaData);
    }

    private Element getElementFromClassType(TypeElement element, String name) {
        boolean isMethod = isMethod(name);
        ElementKind elementKind = isMethod ? ElementKind.METHOD : ElementKind.FIELD;
        if (isMethod) {
            name = name.substring(0, name.length() - 2);
        }
        String finalName = name;
        List<Element> list = elementUtils.getAllMembers(element)
                .stream().filter(elem -> elem.getSimpleName().toString().equals(finalName))
                .filter(elem -> elem.getKind().equals(elementKind))
                .collect(Collectors.toList());
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    private AnnotationMirror getAnnotationMirror(Element element) {
        List<AnnotationMirror> annotationMirrors = elementUtils.getAllAnnotationMirrors(element).stream()
                .filter(annotationMirror -> annotationMirror.getAnnotationType().toString().startsWith(PACKAGE_NAME)
                        && !annotationMirror.getAnnotationType().toString().endsWith(DIALOG_FIELD)
                        && !annotationMirror.getAnnotationType().toString().endsWith(EXTENDS))
                .collect(Collectors.toList());
        if (annotationMirrors.size() == 1) {
            return annotationMirrors.get(0);
        }
        return null;
    }

    private boolean isMethod(String name) {
        return name.endsWith("()");
    }

    private void writeMap(Map<? extends ExecutableElement, ? extends AnnotationValue> map) {
        if (map == null) {
            return;
        }
        try (FileWriter writer = new FileWriter("target\\generated-sources\\annotations\\EXTENDS")) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : map.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeData(ExtendsMetaData extendsMetaData) {
        try (FileWriter writer = new FileWriter("target\\generated-sources\\annotations\\EXTENDS")) {
            Gson gson = new Gson();
            writer.write(gson.toJson(extendsMetaData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
