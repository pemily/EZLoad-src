package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface ActionData {
    EzDataKey share_name = new EzDataKey("ezOperationShareName", "Le nom de la valeur, trouvé grâce à: https://www.boursedirect.fr/api/search/");
    EzDataKey share_ticker = new EzDataKey("ezOperationTicker", "Le code de la valeur, trouvé grâce à: https://www.boursedirect.fr/api/search/");
    EzDataKey share_isin = new EzDataKey("ezOperationISIN", "le code isin de la valeur, trouvé grâce à: https://www.boursedirect.fr/api/search/");

    void fill(EzData data);
}
