package com.pascal.ezload.service.model;

import java.util.Map;

public class EZTaxe extends EZOperation implements IOperationWithAction {
    private EZAction action;

    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.TAXE_SUR_LES_TRANSACTIONS;
    }


    @Override
    public EZAction getAction() {
        return action;
    }

    public void setAction(EZAction action){
        this.action = action;
    }

    public boolean getError() {
        return super.error || action.isError();
    }

    @Override
    protected void fillData(Map<String, String> data) {
        action.fill(data);
    }

}
