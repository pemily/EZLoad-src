package com.pascal.ezload.service.model;

import java.util.Map;

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
    protected void fillData(Map<String, String> data) {
        data.put("operation.prixUnitBrut", prixUnitaireBrut);
        data.put("operation.commission", commission);
        data.put("operation.prelevement", prelevement);
        data.put("operation.montantBrut", amountBrut);
        data.put("operation.creditImpot", creditImpot);
        data.put("operation.contributionSocial", contributionSocial);
        action.fill(data);
    }
}
