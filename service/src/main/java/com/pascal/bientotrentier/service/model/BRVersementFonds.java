package com.pascal.bientotrentier.service.model;

public class BRVersementFonds extends BROperation {
    private BRDevise devise;

    @Override
    public BROperationType getOperationType() {
        return BROperationType.VERSEMENT_FONDS;
    }

    public BRDevise getDevise() {
        return devise;
    }

    public void setDevise(BRDevise devise) {
        this.devise = devise;
    }
}
