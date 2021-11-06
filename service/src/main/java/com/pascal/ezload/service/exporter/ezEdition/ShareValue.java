package com.pascal.ezload.service.exporter.ezEdition;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class ShareValue {
    public static final String LIQUIDITY_CODE = "LIQUIDITE";

    private String tickerCode; // Correspond a la colonne Ticker Google Finance dans MonPortefeuille
    private String userShareName; // can be null if it is not yet filled
    private boolean isDirty; // vrai si depuis la derniere analyse, le user a changé le nom

    public ShareValue(){}
    public ShareValue(String tickerCode, String userShareName, boolean isDirty){
        this.tickerCode = tickerCode;
        this.userShareName = userShareName;
        this.isDirty = isDirty;
        if (tickerCode.equals(LIQUIDITY_CODE) && StringUtils.isBlank(userShareName)){
            this.userShareName = "Liquidité";
        }
        else if (StringUtils.isBlank(userShareName)){
            this.isDirty = true; // on n'accepte pas de valeur vide
        }
    }

    public String getTickerCode() {
        return tickerCode;
    }

    public void setTickerCode(String tickerCode) {
        this.tickerCode = tickerCode;
    }

    public String getUserShareName() {
        return userShareName;
    }

    public void setUserShareName(String userShareName) {
        this.userShareName = userShareName;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShareValue that = (ShareValue) o;
        return tickerCode.equals(that.tickerCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tickerCode);
    }

}
