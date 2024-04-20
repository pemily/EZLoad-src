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

import com.pascal.ezload.service.dashboard.config.RadarChartSettings;

import java.util.List;

public class RadarChart extends RadarChartSettings {

    private List<RadarYearlyChart> radarYearlyCharts;

    public RadarChart(){}
    public RadarChart(RadarChartSettings chartSettings) {
        super(chartSettings);
    }

    public List<RadarYearlyChart> getRadarYearlyCharts() {
        return radarYearlyCharts;
    }

    public void setRadarYearlyCharts(List<RadarYearlyChart> radarYearlyCharts) {
        this.radarYearlyCharts = radarYearlyCharts;
    }


    public static class RadarYearlyChart {
        private int year;
        private List<String> indexLabels; // the index names
        private List<RadarArea> radarAreas;
        private List<ChartLine.Y_AxisSetting> yAxisSetting;
        private List<String> yAxisTitle;

        public List<String> getIndexLabels() {
            return indexLabels;
        }

        public void setIndexLabels(List<String> indexLabels) {
            this.indexLabels = indexLabels;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public List<RadarArea> getRadarAreas() {
            return radarAreas;
        }

        public void setRadarAreas(List<RadarArea> radarAreas) {
            this.radarAreas = radarAreas;
        }

        public List<ChartLine.Y_AxisSetting> getYAxisSetting() {
            return yAxisSetting;
        }

        public void setYAxisSetting(List<ChartLine.Y_AxisSetting> YAxisSetting) {
            this.yAxisSetting = YAxisSetting;
        }

        public List<String> getYAxisTitle() {
            return yAxisTitle;
        }

        public void setYAxisTitle(List<String> yAxisTitle) {
            this.yAxisTitle = yAxisTitle;
        }
    }

    public static class RadarArea {
        private String areaName;
        private String areaGroupId; // will contains: Action/Portefeuille/Devise
        private String borderColor; // rgba(255,99,132,1);
        private String backgroundColor;
        private List<RichValue> datasets;

        public List<RichValue> getDatasets() {
            return datasets;
        }

        public void setDatasets(List<RichValue> datasets) {
            this.datasets = datasets;
        }

        public String getAreaName() {
            return areaName;
        }

        public void setAreaName(String areaName) {
            this.areaName = areaName;
        }

        public String getAreaGroupId() {
            return areaGroupId;
        }

        public void setAreaGroupId(String areaGroupId) {
            this.areaGroupId = areaGroupId;
        }

        public String getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(String borderColor) {
            this.borderColor = borderColor;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

    }
}
