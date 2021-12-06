package com.pascal.ezload.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface EZAccountDeclaration {
    String getName();
    String getNumber();

    @JsonIgnore
    EnumEZBroker getEzBroker();

    boolean isActive();

    void fill(EzData data);
}
