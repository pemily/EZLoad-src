package com.pascal.ezload.service.model;

public class BRDividendeOptionel extends BROperation implements IOperationWithAction {

    private BRAction action;
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
    public BROperationType getOperationType() {
        return BROperationType.DIVIDENDE_VERSE;
    }

    @Override
    public BRAction getAction() {
        return action;
    }

    public void setAction(BRAction action){
        this.action = action;
    }
}
