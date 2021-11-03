package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface MarketPlaceData {
    EzDataKey market_exchange = new EzDataKey("marché.place", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_city = new EzDataKey("marché.ville", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_mic = new EzDataKey("marché.mic", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_acronym = new EzDataKey("marché.acronyme", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_countryCode = new EzDataKey("marché.codePays", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_country = new EzDataKey("marché.pays", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_googleCode = new EzDataKey("marché.codeGoogle", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_currencyCode = new EzDataKey("marché.devise.code", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");
    EzDataKey market_currencySymbol = new EzDataKey("marché.devise.symbole", "Trouvé grâce a l'info mic venant de https://www.boursedirect.fr/api/search/");

    void fill(EzData data);
}
