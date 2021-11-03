package com.pascal.ezload.service.model.operations;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.OperationData;
import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EZOperationType;
import com.pascal.ezload.service.model.IOperationWithAction;

public class EZDividendeOptionel extends EZOperation implements IOperationWithAction {

    private EZAction action;
    private String cours;

    public String getCours() {
        return cours;
    }

    public void setCours(String cours) {
        this.cours = cours;
    }

    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.DIVIDENDE_OPTIONEL;
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
        data.put(OperationData.operation_cours, cours);
    }
}
