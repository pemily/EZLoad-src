package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface MarketPlaceData {
    EzDataKey market_exchange = new EzDataKey("ezMarketPlace", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_city = new EzDataKey("ezMarketCity", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_mic = new EzDataKey("ezMarketMic", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_acronym = new EzDataKey("ezMarketAcronym", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_countryCode = new EzDataKey("ezMarketCountryCode", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_country = new EzDataKey("ezMarketCountry", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_googleCode = new EzDataKey("ezMarketGoogleCode", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_currencyCode = new EzDataKey("ezMarketCurrencyCode", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_currencySymbol = new EzDataKey("ezMarketCurrencySymbol", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");

    void fill(EzData data);
}
