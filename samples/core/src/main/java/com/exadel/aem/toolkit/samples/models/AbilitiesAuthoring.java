package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.meta.NaturalOrdering;

@NaturalOrdering
public interface AbilitiesAuthoring {

    default String a() {
        return "";
    }

    boolean b();

    default int c() {
        return 0;
    }

    default boolean d() {
        return false;
    }

    default int e() {
        return 0;
    }

    boolean f();

    default int g() {
        return 0;
    }

    String h();

    default String i() {
        return "";
    }
}
