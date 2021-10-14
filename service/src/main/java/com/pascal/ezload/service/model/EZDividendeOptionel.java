package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

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
        return EZOperationType.DIVIDENDE_VERSE;
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
        data.put("operation.cours", cours);
    }
}
