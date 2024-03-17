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

public class DashboardPage<T extends ChartSettings> extends Checkable<DashboardPage<T>> {

    private String title;
    private List<T> charts = new LinkedList<>();

    public List<T> getCharts() {
        return charts;
    }

    public void setCharts(List<T> chartSettings) {
        this.charts = chartSettings;
    }

    @Override
    public DashboardPage validate() {
        charts.forEach(ChartSettings::validate);
        return this;
    }

    @Override
    public void clearErrors(){
        charts.forEach(ChartSettings::clearErrors);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
