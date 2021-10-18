package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface ActionData {
    String share_name ="valeur.nom";
    String share_ticker ="valeur.ticker";
    String share_isin ="valeur.isin";

    void fill(EzData data);
}
