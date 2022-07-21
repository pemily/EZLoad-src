package com.pascal.ezload.service.exporter.ezEdition.data.common;

public class SimpleShareValue {
    private final String isin;
    private final String tickerCode;
    private final String userShareName;
    private final String type;

    public SimpleShareValue(String isin, String tickerCode, String userShareName, String type) {
        this.isin = isin;
        this.tickerCode = tickerCode;
        this.userShareName = userShareName;
        this.type = type;
    }

    public String getIsin() {
        return isin;
    }

    public String getTickerCode() {
        return tickerCode;
    }

    public String getUserShareName() {
        return userShareName;
    }

    public String getType() {
        return type;
    }
}
