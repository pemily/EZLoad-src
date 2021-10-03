package com.pascal.ezload.service.model;

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
}
