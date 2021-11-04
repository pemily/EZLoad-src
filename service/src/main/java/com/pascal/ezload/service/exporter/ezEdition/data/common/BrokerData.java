package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface BrokerData {
    EzDataKey broker_name = new EzDataKey("ezBrokerName", "Le nom du courtier");
    EzDataKey broker_version = new EzDataKey("ezBrokerVersion", "La version du courtier");

    void fill(EzData data);
}
