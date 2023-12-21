package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.ChartPerfGroupedBy;
import com.pascal.ezload.service.model.PriceAtDate;

public class PriceForPeriod extends PriceAtDate {

    private ChartPerfGroupedBy groupedBy;

    // si grouped by month => la date sera toujours la dernière du mois
    // si grouped by year => la date sera toujours le 31/12/2023

}
