package com.pascal.ezload.service.model;

public class EZDividendeOptionel extends EZOperation implements IOperationWithAction {

    private EZAction action;
    private int number;
    private String cours;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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
}
