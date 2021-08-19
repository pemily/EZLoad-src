package com.pascal.bientotrentier.model;

public class BRAchat extends BROperation implements IOperationWithAction {

    private BRAction action;
    private int number;
    private String cours;
    private String amountBrut;
    private String fraisCourtage;
    private String tva;

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

    public String getAmountBrut() {
        return amountBrut;
    }

    public void setAmountBrut(String amountBrut) {
        this.amountBrut = amountBrut;
    }

    public String getFraisCourtage() {
        return fraisCourtage;
    }

    public void setFraisCourtage(String fraisCourtage) {
        this.fraisCourtage = fraisCourtage;
    }

    public String getTva() {
        return tva;
    }

    public void setTva(String tva) {
        this.tva = tva;
    }


    @Override
    public BROperationType getOperationType() {
        return BROperationType.ACHAT_TITRES;
    }

    @Override
    public BRAction getAction() {
        return action;
    }

    public void setAction(BRAction action){
        this.action = action;
    }
}
