package com.pascal.ezload.service.model;

public class BRRetraitFonds extends BROperation {
    private BRDevise devise;

    @Override
    public BROperationType getOperationType() {
        return BROperationType.RETRAIT_FONDS;
    }

    public BRDevise getDevise() {
        return devise;
    }

    public void setDevise(BRDevise devise) {
        this.devise = devise;
    }
}
