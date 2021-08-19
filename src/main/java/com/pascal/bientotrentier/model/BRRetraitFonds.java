package com.pascal.bientotrentier.model;

public class BRRetraitFonds extends BROperation {

    @Override
    public BROperationType getOperationType() {
        return BROperationType.RETRAIT_FONDS;
    }

}
