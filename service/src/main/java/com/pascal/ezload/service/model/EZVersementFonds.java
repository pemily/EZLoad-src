package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public class EZVersementFonds extends EZOperation {
    private EZDevise devise;

    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.VERSEMENT_FONDS;
    }

    public EZDevise getDevise() {
        return devise;
    }

    public void setDevise(EZDevise devise) {
        this.devise = devise;
    }

    @Override
    protected void fillData(EzData data) {
        data.put("operation.codeDevise", devise.getCode());
        data.put("operation.symbolDevise", devise.getSymbol());
    }
}
