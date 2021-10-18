package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface ReportData {

    String report_source ="rapport.source";
    String report_date ="rapport.date";

    void fill(EzData data);
}
