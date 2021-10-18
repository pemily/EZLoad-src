package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.BourseDirectV1Data;

public class EZVersementFonds extends EZOperation implements BourseDirectV1Data {
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
        data.put(operation_codeDevise, devise.getCode());
        data.put(operation_symbolDevise, devise.getSymbol());
    }
}
