package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public class EZDroitDeGarde extends EZOperation {
    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.DROITS_DE_GARDE_OU_FRAIS_DIVERS;
    }

    @Override
    protected void fillData(EzData data) {
        // no additional data
    }

}
