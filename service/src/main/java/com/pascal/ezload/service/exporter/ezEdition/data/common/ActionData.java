package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface ActionData {
    EzDataKey share_rawName = new EzDataKey("ezOperationShareRawName", "Le nom de la valeur, trouvé grâce à: https://www.boursedirect.fr/api/search/");
    EzDataKey share_ticker = new EzDataKey("ezOperationTicker", "Le code de la valeur, trouvé grâce à: https://www.boursedirect.fr/api/search/");
    EzDataKey share_isin = new EzDataKey("ezOperationISIN", "le code isin de la valeur, trouvé grâce à: https://www.boursedirect.fr/api/search/");
    EzDataKey share_ezName = new EzDataKey("ezOperationShareName", "Le nom choisi par l'utilisateur de la valeur");
    EzDataKey share_ezCode = new EzDataKey("ezShareCode", "Le ticker Google Finance de la valeur, construit avec ezMarketGoogleCode:ezOperationTicker");
    EzDataKey share_costPrice = new EzDataKey("ezPRU", "Le PRU calculé dans l'onglet PRU de ezPortfolio");
    void fill(EzData data);
}
