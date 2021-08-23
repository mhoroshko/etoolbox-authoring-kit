package com.exadel.aem.toolkit.plugin.processor;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
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

import org.apache.commons.lang3.ClassUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
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

    private ClassInfo processElement(Element element) {
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
        return classInfo;
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

    public static <T extends Annotation> T createInstance(Class<T> type, Map<String, Object> values) {
        Map<String, BiFunction<Annotation, Object[], Object>> methods = new HashMap<>();
        for (Method method : type.getDeclaredMethods()) {
            BiFunction<Annotation, Object[], Object> methodFunction = (src, args) -> {
                if (values != null && values.containsKey(method.getName())) {
                    Object o = values.get(method.getName());
                    if (o.getClass().isArray() && ((Object[]) o)[0] instanceof AnnotationInfo) {
                        AnnotationInfo annotationInfo = (AnnotationInfo) ((Object[]) o)[0];
                        return castToArray(Arrays.stream(((Object[]) o))
                            .map(AnnotationInfo.class::cast)
                            .map(qwe -> createInstance(getClass(qwe), qwe.getValues()))
                            .toArray(), getClass(annotationInfo));
                    }
                    if (o instanceof AnnotationInfo) {
                        return createInstance(getClass((AnnotationInfo) o), ((AnnotationInfo) o).getValues());
                    } else {
                        return o;
                    }
                }
                return method.getDefaultValue();
            };
            methods.put(method.getName(), methodFunction);
        }
        return genericModify(null, type, methods);
    }

    private static Class getClass(AnnotationInfo annotationInfo) {
        try {
            return Class.forName(annotationInfo.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T, U, R> U genericModify(T value, Class<U> modification, Map<String, BiFunction<T, Object[], R>> methods) {
        if (modification == null) {
            return null;
        }
        if (methods == null || methods.isEmpty()) {
            try {
                return modification.cast(value);
            } catch (ClassCastException e) {
                return null;
            }
        }
        Object result = Proxy.newProxyInstance(modification.getClassLoader(),
            new Class[]{modification},
            new AnnotationUtil.ExtensionInvocationHandler<>(value, modification, methods));
        return modification.cast(result);
    }

    private static <T, U> U[] castToArray(T[] source, Class<U> targetType) {
        U[] result = (U[]) Array.newInstance(targetType, source.length);
        for (int i = 0; i < source.length; i++) {
            result[i] = (U) source[i];
        }
        return result;
    }
}
