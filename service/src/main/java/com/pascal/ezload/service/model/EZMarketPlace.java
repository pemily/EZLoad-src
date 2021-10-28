package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.MarketPlaceData;

public class EZMarketPlace implements MarketPlaceData {

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

    public void fill(EzData data) {
        data.put(market_exchange, stockExchange);
        data.put(market_city, city);
        data.put(market_mic, mic);
        data.put(market_acronym, acronym);
        data.put(market_countryCode, country.getCode());
        data.put(market_country, country.getName());
        data.put(market_googleCode, googleFinanceCode);
        data.put(market_currencyCode, currency.getCode());
        data.put(market_currencySymbol, currency.getSymbol());
    }
}
