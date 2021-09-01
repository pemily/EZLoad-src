package com.pascal.ezload.service.model;

public class BRVenteEtranger extends BRVente {

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
        return BROperationType.VENTE_TITRES;
    }

}
