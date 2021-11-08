package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface OperationData {

    String EZOperationDesignation = "ezOperation_INFO";

    // common info in all operations
    EzDataKey operation_date = new EzDataKey("ezOperation_DATE", "la date de l'opération");
    EzDataKey operation_ezLiquidityName= new EzDataKey("ezLiquidityName", "Le nom de la valeur: LIQUIDITE");

    void fill(EzData data);
}
