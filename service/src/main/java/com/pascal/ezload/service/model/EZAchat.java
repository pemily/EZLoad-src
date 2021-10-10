package com.pascal.ezload.service.model;

import java.util.Map;

public class EZAchat extends EZOperation implements IOperationWithAction {

    private EZAction action;
    private int quantite;
    private String cours;
    private String amountBrut;
    private String fraisCourtage;
    private String tva;

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
        return EZOperationType.ACHAT_TITRES;
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
        data.put("operation.montantBrut", amountBrut);
        data.put("operation.fraisCourtage", fraisCourtage);
        data.put("operation.tva", tva);
    }

}
