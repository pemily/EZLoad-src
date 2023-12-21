package com.pascal.ezload.service.dashboard.config;

public class ChartPortfolioIndexConfig {
    private PortfolioIndex portfolioIndex;

    private ChartPerfSettings perfSettings; // peux etre null

    public PortfolioIndex getPortfolioIndex() {
        return portfolioIndex;
    }

    public void setPortfolioIndex(PortfolioIndex portfolioIndex) {
        this.portfolioIndex = portfolioIndex;
    }

    public ChartPerfSettings getPerfSettings() {
        return perfSettings;
    }

    public void setPerfSettings(ChartPerfSettings perfSettings) {
        this.perfSettings = perfSettings;
    }
}
