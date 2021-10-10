package com.pascal.ezload.service.model;

import java.util.Map;

public class EZEspeceSurOST extends EZOperation implements IOperationWithAction {

    private EZAction action;

    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.DIVIDENDE_VERSE; // ?? Pas sûr de moi ??
    }

    @Override
    public EZAction getAction() {
        return action;
    }

    public void setAction(EZAction action){
        this.action = action;
    }

    public boolean hasError() {
        return super.error || action.isError();
    }

    @Override
    protected void fillData(Map<String, String> data) {
        action.fill(data);
    }

}
