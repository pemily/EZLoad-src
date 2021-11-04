package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface AccountData {

    EzDataKey account_name = new EzDataKey("ezAccountName", "Viens du site BourseDirect");
    EzDataKey account_number = new EzDataKey("ezAccountNumber");
    EzDataKey account_type = new EzDataKey("ezAccountType");
    EzDataKey account_owner_name = new EzDataKey("ezAccountOwnerName", "Le nom du propriétaire du compte");
    EzDataKey account_owner_address = new EzDataKey("ezAccountOwnerAddress", "L'adresse du propriétaire du compte");
    EzDataKey account_devise_symbol = new EzDataKey("ezAccountCurrencySymbol", "Le symbol de la devise du compte");
    EzDataKey account_devise_code = new EzDataKey("ezAccountCurrencyCode", "Le code de la devise du compte");

    void fill(EzData data);
}
