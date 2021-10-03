package com.pascal.ezload.service.model;

public class EZAction {
    private String name;
    private String ticker;
    private String isin;
    private EZMarketPlace marketPlace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public EZMarketPlace getMarketPlace() {
        return marketPlace;
    }

    public void setMarketPlace(EZMarketPlace marketPlace) {
        this.marketPlace = marketPlace;
    }
}
