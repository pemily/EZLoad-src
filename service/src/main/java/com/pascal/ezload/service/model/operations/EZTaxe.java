package com.pascal.ezload.service.model.operations;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EZOperationType;
import com.pascal.ezload.service.model.IOperationWithAction;

public class EZTaxe extends EZOperation implements IOperationWithAction {
    private EZAction action;

    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.TAXE;
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
