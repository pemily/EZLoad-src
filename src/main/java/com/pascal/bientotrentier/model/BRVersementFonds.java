package com.pascal.bientotrentier.model;

public class BRVersementFonds extends BROperation {


    @Override
    public BROperationType getOperationType() {
        return BROperationType.VERSEMENT_FONDS;
    }

}
