package com.pascal.ezload.service.model;

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
}
