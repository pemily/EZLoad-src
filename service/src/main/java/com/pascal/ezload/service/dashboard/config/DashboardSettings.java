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
