package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface AccountData {

    EzDataKey account_name = new EzDataKey("compte.nom", "Viens du site BourseDirect");
    EzDataKey account_number = new EzDataKey("compte.numero");
    EzDataKey account_type = new EzDataKey("compte.type");
    EzDataKey account_owner_name = new EzDataKey("compte.proprietaire.nom", "Viens de l'avis d'opération");
    EzDataKey account_owner_address = new EzDataKey("compte.proprietaire.adresse", "Viens de l'avis d'opération");
    EzDataKey account_devise_symbol = new EzDataKey("compte.devise.symbole", "Viens de l'avis d'opération");
    EzDataKey account_devise_code = new EzDataKey("compte.devise.code", "Viens de l'avis d'opération");

    void fill(EzData data);
}
