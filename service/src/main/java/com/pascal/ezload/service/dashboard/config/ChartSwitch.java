/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.dashboard.config;

import com.pascal.ezload.service.dashboard.ImpotChart;
import com.pascal.ezload.service.dashboard.RadarChart;
import com.pascal.ezload.service.dashboard.SolarChart;
import com.pascal.ezload.service.dashboard.TimeLineChart;

public class ChartSwitch {

    // only one of this
    private TimeLineChart timeLine;
    private RadarChart radar;
    private SolarChart portfolioSolar;
    private ImpotChart impot;

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
        else if (impot != null)
            impot.validate();
    }

    public void clearErrors() {
        if (timeLine != null)
            timeLine.clearErrors();
        else if (radar != null)
            radar.clearErrors();
        else if (portfolioSolar != null)
            portfolioSolar.clearErrors();
        else if (impot != null)
            impot.clearErrors();
    }

    public ImpotChart getImpot() {
        return impot;
    }

    public void setImpot(ImpotChart impotChart) {
        this.impot = impotChart;
    }
}
