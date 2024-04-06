package com.pascal.ezload.service.dashboard.config;

public interface DashboardChart {

    enum ChartType {
        TIMELINE,
        RADAR
    }

    String getTitle();

    void setTitle(String title);

    ChartType type();

    <T extends DashboardChart> T validate();

    void clearErrors();
}
