package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

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

    @Override
    protected void fillData(EzData data) {
        action.fill(data);
    }

}
