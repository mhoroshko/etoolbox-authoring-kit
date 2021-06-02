package com.exadel.aem.toolkit.core.processor;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.exadel.aem.toolkit.api.annotations.meta.NaturalOrdering;

@SupportedAnnotationTypes("com.exadel.aem.toolkit.api.annotations.meta.NaturalOrdering")
public class NaturalOrderingProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        System.out.println("!!!!!!!!!!!!!!Natural Ordering!!!!!!!!!!!!!!");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(NaturalOrdering.class);

        set.forEach(element -> {
            System.out.println("-----");
            System.out.println(elementUtils.getPackageOf(element).getQualifiedName().toString() + "." + element.getSimpleName().toString());
            element.getEnclosedElements().stream().map(Element::getSimpleName).forEach(System.out::println);
            System.out.println("-----");
        });
        return true;
    }
}
