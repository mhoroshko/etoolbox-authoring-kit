package com.exadel.aem.toolkit.plugin.processor;

import java.util.LinkedList;
import java.util.List;

abstract class BaseInfo {

    private final String name;
    private final List<AnnotationInfo> annotationInfos;

    BaseInfo(String name) {
        this.name = name;
        this.annotationInfos = new LinkedList<>();
    }

    public void addAnnotationInfo(AnnotationInfo annotationInfo) {
        this.annotationInfos.add(annotationInfo);
    }
}
