package com.pascal.ezload.service.model;

import java.util.Map;

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

    public void fill(Map<String, String> data) {
        data.put("action.name", name);
        data.put("action.ticker", ticker);
        data.put("action.isin", isin);
        marketPlace.fill(data);
    }
}
