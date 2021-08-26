package com.pascal.bientotrentier.service.model;

public class BREspeceSurOST extends BROperation implements IOperationWithAction {

    private BRAction action;

    @Override
    public BROperationType getOperationType() {
        return BROperationType.DIVIDENDE_VERSE; // ?? Pas sûr de moi ??
    }

    @Override
    public BRAction getAction() {
        return action;
    }

    public void setAction(BRAction action){
        this.action = action;
    }
}
