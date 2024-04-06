package com.pascal.ezload.service.dashboard.config;

public interface DashboardChart {

    enum ChartType {
        TIMELINE,
        RADAR
    }

    String getTitle();

    void setTitle(String title);

    ChartType getType();

    void setType(ChartType chartType);


    <T extends DashboardChart> T validate();

    void clearErrors();
}
