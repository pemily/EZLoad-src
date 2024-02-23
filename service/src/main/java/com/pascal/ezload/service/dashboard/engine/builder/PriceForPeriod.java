package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.ChartGroupedBy;
import com.pascal.ezload.service.model.PriceAtDate;

// This class is not used, should I use it????
// TODO voir PerfIndexBuilder.computePerf
public class PriceForPeriod extends PriceAtDate {

    private ChartGroupedBy groupedBy;

    // si grouped by month => la date sera toujours la dernière du mois
    // si grouped by year => la date sera toujours le 31/12/2023

}
