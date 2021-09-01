package com.pascal.ezload.service.model;

public class BRTaxe extends BROperation implements IOperationWithAction {
    private BRAction action;

    @Override
    public BROperationType getOperationType() {
        return BROperationType.TAXE_SUR_LES_TRANSACTIONS;
    }


    @Override
    public BRAction getAction() {
        return action;
    }

    public void setAction(BRAction action){
        this.action = action;
    }
}
