package com.pascal.ezload.service.model;

public class EZVente extends EZOperation implements IOperationWithAction {

    private EZAction action;
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
    public EZOperationType getOperationType() {
        return EZOperationType.VENTE_TITRES;
    }

    @Override
    public EZAction getAction() {
        return action;
    }

    public void setAction(EZAction action){
        this.action = action;
    }
}
