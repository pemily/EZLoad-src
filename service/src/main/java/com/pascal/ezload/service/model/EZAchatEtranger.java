package com.pascal.ezload.service.model;

public class EZAchatEtranger extends EZAchat {

    private String changeRate;
    private String coursUSD;

    public String getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(String changeRate) {
        this.changeRate = changeRate;
    }

    public String getCoursUSD() {
        return coursUSD;
    }

    public void setCoursUSD(String coursUSD) {
        this.coursUSD = coursUSD;
    }

    @Override
    public EZOperationType getOperationType() {
        return EZOperationType.ACHAT_TITRES;
    }

}
