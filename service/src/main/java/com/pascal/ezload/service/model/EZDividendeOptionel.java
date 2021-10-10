package com.pascal.ezload.service.model;

import java.util.Map;

public class EZDividendeOptionel extends EZOperation implements IOperationWithAction {

    private EZAction action;
    private int quantite;
    private String cours;

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

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

    public boolean hasError() {
        return super.error || action.isError();
    }

    @Override
    protected void fillData(Map<String, String> data) {
        action.fill(data);
        data.put("operation.quantite", quantite+"");
        data.put("operation.cours", cours);
    }
}
