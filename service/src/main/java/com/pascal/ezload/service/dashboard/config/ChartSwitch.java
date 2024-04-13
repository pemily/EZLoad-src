package com.pascal.ezload.service.dashboard.config;

import com.pascal.ezload.service.dashboard.RadarChart;
import com.pascal.ezload.service.dashboard.SolarChart;
import com.pascal.ezload.service.dashboard.TimeLineChart;

public class ChartSwitch {

    // only one of this
    private TimeLineChart timeLine;
    private RadarChart radar;
    private SolarChart portfolioSolar;

    public TimeLineChart getTimeLine() {
        return timeLine;
    }

    public void setTimeLine(TimeLineChart timeLine) {
        this.timeLine = timeLine;
    }

    public RadarChart getRadar() {
        return radar;
    }

    public void setRadar(RadarChart radar) {
        this.radar = radar;
    }

    public SolarChart getPortfolioSolar() {
        return portfolioSolar;
    }

    public void setPortfolioSolar(SolarChart solar) {
        this.portfolioSolar = solar;
    }

    public void validate() {
        if (timeLine != null)
            timeLine.validate();
        else if (radar != null)
            radar.validate();
        else if (portfolioSolar != null)
            portfolioSolar.validate();
    }

    public void clearErrors() {
        if (timeLine != null)
            timeLine.clearErrors();
        else if (radar != null)
            radar.clearErrors();
        else if (portfolioSolar != null)
            portfolioSolar.clearErrors();
    }

}
