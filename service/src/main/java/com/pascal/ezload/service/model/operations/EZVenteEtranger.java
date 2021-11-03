package com.pascal.ezload.service.model.operations;

import com.pascal.ezload.service.model.EZOperationType;

public class EZVenteEtranger extends EZVente {

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
        return EZOperationType.VENTE_TITRES_ETRANGER;
    }

}
