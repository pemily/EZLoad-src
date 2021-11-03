package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface BrokerData {
    EzDataKey broker_name = new EzDataKey("courtier.nom");
    EzDataKey broker_version = new EzDataKey("courtier.version");
    EzDataKey broker_dir = new EzDataKey("courtier.dossier");

    void fill(EzData data);
}
