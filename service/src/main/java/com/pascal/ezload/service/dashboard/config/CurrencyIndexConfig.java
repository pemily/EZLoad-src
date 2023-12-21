package com.pascal.ezload.service.dashboard.config;

public class CurrencyIndexConfig {
    // all the devises found in the charts will be automatically added

    private ChartPerfSettings perfSettings; // can be null

    public ChartPerfSettings getPerfSettings() {
        return perfSettings;
    }

    public void setPerfSettings(ChartPerfSettings perfSettings) {
        this.perfSettings = perfSettings;
    }
}
