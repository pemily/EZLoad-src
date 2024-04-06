package com.pascal.ezload.service.dashboard.config;

import com.pascal.ezload.service.util.Checkable;

public class RadarChartSettings extends Checkable<RadarChartSettings> implements DashboardChart  {

    private String title;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public ChartType type() {
        return ChartType.RADAR;
    }


    public RadarChartSettings validate() {
        return this;
    }
}
