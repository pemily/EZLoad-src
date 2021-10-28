package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface MarketPlaceData {
    String market_exchange = "marche.place";
    String market_city = "marche.ville";
    String market_mic = "marche.mic";
    String market_acronym = "marche.acronyme";
    String market_countryCode = "marche.codePays";
    String market_country = "marche.pays";
    String market_googleCode = "marche.codeGoogle";
    String market_currencyCode = "marche.devise.code";
    String market_currencySymbol = "marche.devise.symbole";

    void fill(EzData data);
}
