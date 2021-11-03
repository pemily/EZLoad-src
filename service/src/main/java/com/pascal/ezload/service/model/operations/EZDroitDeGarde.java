package com.pascal.ezload.service.model.operations;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EZOperationType;

public class EZDroitDeGarde extends EZOperation {
    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.DROITS_DE_GARDE;
    }

    @Override
    protected void fillData(EzData data) {
        // no additional data
    }

}
