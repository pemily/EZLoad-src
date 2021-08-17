package com.pascal.bientotrentier.model;

public class BRCoupons extends BROperation {
    private String id;
    private String actionName;
    private int number;
    private String prixUnitaireBrut;
    private String commission;
    private String prelevement;
    private String amountBrut;
    private String creditImpot;
    private String contributionSocial;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

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
}
