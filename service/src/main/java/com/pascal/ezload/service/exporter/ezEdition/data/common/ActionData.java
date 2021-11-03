package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface ActionData {
    EzDataKey share_name = new EzDataKey("operation.valeur.nom", "Trouvé grâce à: https://www.boursedirect.fr/api/search/");
    EzDataKey share_ticker = new EzDataKey("operation.valeur.ticker", "Trouvé grâce à: https://www.boursedirect.fr/api/search/");
    EzDataKey share_isin = new EzDataKey("operation.valeur.isin", "Trouvé grâce à: https://www.boursedirect.fr/api/search/");

    void fill(EzData data);
}
