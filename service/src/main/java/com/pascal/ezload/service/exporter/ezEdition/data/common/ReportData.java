package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface ReportData {

    EzDataKey report_source = new EzDataKey("ezReportSource", "le fichier de l'avis d'opération"); // ne pas mettre le path complet car si le compte est renommé cela ne marchera pas dans MesOperations.isOperationsExists

    void fill(EzData data);
}
