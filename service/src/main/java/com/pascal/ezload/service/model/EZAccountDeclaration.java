package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface EZAccountDeclaration {
    String getName();
    boolean isActive();

    void fill(EzData data);
}
