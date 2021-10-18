package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface MarketPlaceData {
    String market_exchange = "marché.place";
    String market_city = "marché.ville";
    String market_mic = "marché.mic";
    String market_acronym = "marché.acronyme";
    String market_countryCode = "marché.codePays";
    String market_country = "marché.pays";
    String market_googleTicker = "marché.tickerGoogle";
    String market_currencyCode = "marché.devise.code";
    String market_currencySymbol = "marché.devise.symbole";

    void fill(EzData data);
}
