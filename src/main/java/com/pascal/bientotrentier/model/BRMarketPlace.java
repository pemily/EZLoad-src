package com.pascal.bientotrentier.model;

public class BRMarketPlace {

    private String stockExchange;
    private String city;
    private String mic;
    private String acronym;
    private BRCountry country;
    private String googleFinanceCode;
    private BRDevise currency;

    public BRMarketPlace(String stockExchange, String city, String mic, String acronym, String googleFinanceCode, BRCountry country, BRDevise currency) {
        this.stockExchange = stockExchange;
        this.city = city;
        this.mic = mic;
        this.acronym = acronym;
        this.googleFinanceCode = googleFinanceCode;
        this.country = country;
        this.currency = currency;
    }

    public String getStockExchange() {
        return stockExchange;
    }

    public String getCity() {
        return city;
    }

    public String getMic() {
        return mic;
    }

    public String getAcronym() {
        return acronym;
    }

    public BRCountry getCountry() {
        return country;
    }

    public BRDevise getCurrency() {
        return currency;
    }

    public String getGoogleFinanceCode() {
        return googleFinanceCode;
    }
}
