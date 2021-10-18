package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface BrokerData {
    String broker_name = "courtier.nom";
    String broker_version = "courtier.version";
    String broker_dir = "courtier.dossier";

    void fill(EzData data);
}
