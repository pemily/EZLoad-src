package com.pascal.ezload.service.model.operations;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EZOperationType;
import com.pascal.ezload.service.model.IOperationWithAction;

public class EZCoupons extends EZOperation implements IOperationWithAction {

    private EZAction action;
    private String prixUnitaireBrut;
    private String commission;
    private String prelevement;
    private String amountBrut;
    private String creditImpot;
    private String contributionSocial;

    public String getPrixUnitaireBrut() {
        return prixUnitaireBrut;
    }

    public void setPrixUnitaireBrut(String prixUnitaireBrut) {
        this.prixUnitaireBrut = prixUnitaireBrut;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    public String getPrelevement() {
        return prelevement;
    }

    public void setPrelevement(String prelevement) {
        this.prelevement = prelevement;
    }

    public String getAmountBrut() {
        return amountBrut;
    }

    public void setAmountBrut(String amountBrut) {
        this.amountBrut = amountBrut;
    }

    public String getCreditImpot() {
        return creditImpot;
    }

    public void setCreditImpot(String creditImpot) {
        this.creditImpot = creditImpot;
    }

    public String getContributionSocial() {
        return contributionSocial;
    }

    public void setContributionSocial(String contributionSocial) {
        this.contributionSocial = contributionSocial;
    }

    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.COUPONS;
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
        data.put(operation_prixUnitBrut, prixUnitaireBrut);
        data.put(operation_commission, commission);
        data.put(operation_prelevement, prelevement);
        data.put(operation_montantBrut, amountBrut);
        data.put(operation_creditImpot, creditImpot);
        data.put(operation_contributionSocial, contributionSocial);
        action.fill(data);
    }
}
