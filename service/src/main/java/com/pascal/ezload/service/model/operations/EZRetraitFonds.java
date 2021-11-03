package com.pascal.ezload.service.model.operations;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EZOperationType;

public class EZRetraitFonds extends EZOperation {
    private EZDevise devise;

    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.RETRAIT_FONDS;
    }

    public EZDevise getDevise() {
        return devise;
    }

    public void setDevise(EZDevise devise) {
        this.devise = devise;
    }

    @Override
    protected void fillData(EzData data) {
        data.put(operation_codeDevise, devise.getCode());
        data.put(operation_symbolDevise, devise.getSymbol());
    }

}
