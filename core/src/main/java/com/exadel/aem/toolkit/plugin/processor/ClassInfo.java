package com.exadel.aem.toolkit.plugin.processor;

import java.util.LinkedList;
import java.util.List;

public class ClassInfo extends BaseInfo {

    private List<MemberInfo> memberInfos;

    public ClassInfo(String name) {
        super(name);
        this.memberInfos = new LinkedList<>();
    }

    public void addMemberInfo(MemberInfo memberInfo) {
        this.memberInfos.add(memberInfo);
    }
}
