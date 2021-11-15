package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.ActionData;

public class EZAction implements ActionData {
    private String ezName; // the name from the user preference
    private String rawName; // the name from the site BourseDirect https://www.boursedirect.fr/api/search/
    private String ezTicker; // the full name = marketPlace.googleFinanceCode + ticker, example: NYSE:WPC, EPA:RUI
    private String ticker; // WPC, EPA
    private String isin;
    private String pruCellReference;
    private EZMarketPlace marketPlace; // the marketPlace.googleFinanceCode contains: NYSE, EPA
    private String type;

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
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

    public String getEzName() {
        return ezName;
    }

    public void setEzName(String ezName) {
        this.ezName = ezName;
    }

    public String getEzTicker() {
        return ezTicker;
    }

    public void setEzTicker(String ezTicker) {
        this.ezTicker = ezTicker;
    }

    public String getPruCellReference() {
        return pruCellReference;
    }

    public void setPruCellReference(String pruCellReference) {
        this.pruCellReference = pruCellReference;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void fill(EzData data) {
        data.put(share_rawName, rawName);
        data.put(share_ticker, ticker);
        data.put(share_isin, isin);
        data.put(share_ezName, ezName);
        data.put(share_ezCode, ezTicker);
        data.put(share_costPrice, pruCellReference);
        data.put(share_type, type);
        marketPlace.fill(data);
    }

}
