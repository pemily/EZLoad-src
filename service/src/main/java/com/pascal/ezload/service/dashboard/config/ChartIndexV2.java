package com.pascal.ezload.service.dashboard.config;

public class ChartIndexV2 {

    // It's a oneOf (only one of those value below are set, others are null)
    private ChartPortfolioIndexConfig portfolioIndexConfig;
    private ChartShareIndexConfig shareIndexConfig;
    private CurrencyIndexConfig currencyIndexConfig; // all currencies found will be shown

    public ChartPortfolioIndexConfig getPortfolioIndexConfig() {
        return portfolioIndexConfig;
    }

    public void setPortfolioIndexConfig(ChartPortfolioIndexConfig portfolioIndexConfig) {
        this.portfolioIndexConfig = portfolioIndexConfig;
    }

    public ChartShareIndexConfig getShareIndexConfig() {
        return shareIndexConfig;
    }

    public void setShareIndexConfig(ChartShareIndexConfig shareIndexConfig) {
        this.shareIndexConfig = shareIndexConfig;
    }

    public CurrencyIndexConfig getCurrencyIndexConfig() {
        return currencyIndexConfig;
    }

    public void setCurrencyIndexConfig(CurrencyIndexConfig currencyIndexConfig) {
        this.currencyIndexConfig = currencyIndexConfig;
    }
}
