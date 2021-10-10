package com.pascal.ezload.service.model;

import java.util.Map;

public class EZDroitDeGarde extends EZOperation {
    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.DROITS_DE_GARDE_OU_FRAIS_DIVERS;
    }

    @Override
    protected void fillData(Map<String, String> data) {
        // no additional data
    }

}
