package com.pascal.bientotrentier.service.model;

public class BRAchatEtranger extends BRAchat {

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
    public BROperationType getOperationType() {
        return BROperationType.ACHAT_TITRES;
    }

}
