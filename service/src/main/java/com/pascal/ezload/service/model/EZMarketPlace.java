package com.pascal.ezload.service.model;

import java.util.Map;

public class EZMarketPlace {

    private String stockExchange;
    private String city;
    private String mic;
    private String acronym;
    private EZCountry country;
    private String googleFinanceCode;
    private EZDevise currency;

    public EZMarketPlace(String stockExchange, String city, String mic, String acronym, String googleFinanceCode, EZCountry country, EZDevise currency) {
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

    public EZCountry getCountry() {
        return country;
    }

    public EZDevise getCurrency() {
        return currency;
    }

    public String getGoogleFinanceCode() {
        return googleFinanceCode;
    }

    public void fill(Map<String, String> data) {
        data.put("marché.echange", stockExchange);
        data.put("marché.ville", city);
        data.put("marché.mic", mic);
        data.put("marché.acronym", acronym);
        data.put("marché.codePays", country.getCode());
        data.put("marché.pays", country.getName());
        data.put("marché.googleFinanceCode", googleFinanceCode);
        data.put("marché.codeDevise", currency.getCode());
        data.put("marché.symbolDevise", currency.getSymbol());
    }
}
