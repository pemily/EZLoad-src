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

import com.pascal.ezload.common.util.Checkable;

import java.util.LinkedList;
import java.util.List;

public class DashboardPage extends Checkable<DashboardPage> {

    private String title;
    private List<ChartSwitch> charts = new LinkedList<>();

    public List<ChartSwitch> getCharts() {
        return charts;
    }

    public void setCharts(List<ChartSwitch> chartSettings) {
        this.charts = chartSettings;
    }

    @Override
    public DashboardPage validate() {
        charts.forEach(ChartSwitch::validate);
        return this;
    }

    @Override
    public void clearErrors(){
        charts.forEach(ChartSwitch::clearErrors);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
