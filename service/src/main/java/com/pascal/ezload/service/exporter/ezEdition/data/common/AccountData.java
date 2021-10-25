package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface AccountData {

    String account_name = "compte.nom";
    String account_number = "compte.numero";
    String account_type = "compte.type";
    String account_owner_name = "compte.proprietaire.nom";
    String account_owner_address = "compte.proprietaire.adresse";
    String account_devise_symbol = "compte.devise.symbole";
    String account_devise_code = "compte.devise.code";

    void fill(EzData data);
}
