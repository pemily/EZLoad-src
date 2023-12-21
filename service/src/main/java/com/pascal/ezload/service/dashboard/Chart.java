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
package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.dashboard.ChartLine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Chart {

    private String mainTitle;
    private Map<String, String> axisId2titleX = new HashMap<>(), axisId2titleY = new HashMap<>();
    private List<Object> labels = new LinkedList<>();
    private List<ChartLine> lines = new LinkedList<>();

    public List<Object> getLabels() {
        return labels;
    }

    public void setLabels(List<Object> labels) {
        this.labels = labels;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }

    public Map<String, String> getAxisId2titleX() {
        return axisId2titleX;
    }

    public void setAxisId2titleX(Map<String, String> axisId2titleX) {
        this.axisId2titleX = axisId2titleX;
    }

    public Map<String, String> getAxisId2titleY() {
        return axisId2titleY;
    }

    public void setAxisId2titleY(Map<String, String> axisId2titleY) {
        this.axisId2titleY = axisId2titleY;
    }

    public List<ChartLine> getLines() {
        return lines;
    }

    public void setLines(List<ChartLine> lines) {
        this.lines = lines;
    }
}
