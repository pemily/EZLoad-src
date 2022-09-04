package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.util.Checkable;

import java.util.LinkedList;
import java.util.List;

public class DashboardSettings extends Checkable<DashboardSettings> {

    private List<ChartSettings> chartSettings = new LinkedList<>();

    public List<ChartSettings> getChartSettings() {
        return chartSettings;
    }

    public void setChartSettings(List<ChartSettings> chartSettings) {
        this.chartSettings = chartSettings;
    }

    @Override
    public DashboardSettings validate() {
        chartSettings.forEach(ChartSettings::validate);
        return this;
    }

    @Override
    public void clearErrors(){
        chartSettings.forEach(ChartSettings::clearErrors);
    }
}
