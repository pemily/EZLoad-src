package com.pascal.ezload.service.dashboard;

import java.util.List;

public class DashboardData {

    private List<Chart> charts;
    private DashboardSettings dashboardSettings;

    public List<Chart> getCharts() {
        return charts;
    }

    public void setCharts(List<Chart> charts) {
        this.charts = charts;
    }

    public DashboardSettings getDashboardSettings() {
        return dashboardSettings;
    }

    public void setDashboardSettings(DashboardSettings dashboardSettings) {
        this.dashboardSettings = dashboardSettings;
    }
}
