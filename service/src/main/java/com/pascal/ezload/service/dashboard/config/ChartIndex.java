package com.pascal.ezload.service.dashboard.config;

public class ChartIndex {

    private String id;
    private String label;
    private String description;
    private GraphStyle graphStyle = GraphStyle.LINE;

    // It's a oneOf (only one of those value below are set, others are null)
    private ChartPortfolioIndexConfig portfolioIndexConfig;
    private ChartShareIndexConfig shareIndexConfig;
    private CurrencyIndexConfig currencyIndexConfig; // all currencies found will be shown
    private ChartPerfSettings perfSettings; // peux etre null

    public ChartPerfSettings getPerfSettings() {
        return perfSettings;
    }

    public void setPerfSettings(ChartPerfSettings perfSettings) {
        this.perfSettings = perfSettings;
    }

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GraphStyle getGraphStyle() {
        return graphStyle;
    }

    public void setGraphStyle(GraphStyle graphStyle) {
        this.graphStyle = graphStyle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
