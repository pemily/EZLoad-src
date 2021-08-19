package com.pascal.bientotrentier.model;

public class BRAction {
    private String name;
    private String ticker;
    private String isin;
    private String country;
    private BRDevise devise;
    private String marketMic;
    private String marketName;

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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMarketMic() {
        return marketMic;
    }

    public void setMarketMic(String marketMic) {
        this.marketMic = marketMic;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public BRDevise getDevise() {
        return devise;
    }

    public void setDevise(BRDevise devise) {
        this.devise = devise;
    }
}
