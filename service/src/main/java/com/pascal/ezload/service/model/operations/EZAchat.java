package com.pascal.ezload.service.model.operations;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EZOperationType;
import com.pascal.ezload.service.model.IOperationWithAction;

public class EZAchat extends EZOperation implements IOperationWithAction {

    private EZAction action;
    private String cours;
    private String amountBrut;
    private String fraisCourtage;
    private String tva;

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

    @Override
    protected void fillData(EzData data) {
        action.fill(data);
        data.put(operation_cours, cours);
        data.put(operation_montantBrut, amountBrut);
        data.put(operation_fraisCourtage, fraisCourtage);
        data.put(operation_tva, tva);
    }

}
