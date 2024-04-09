package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.dashboard.config.RadarChartSettings;

import java.util.List;

public class SolarChart extends RadarChartSettings {

    private List<SolarYearlyChart> solarYearlyCharts;

    public SolarChart(){}
    public SolarChart(RadarChartSettings chartSettings) {
        super(chartSettings);
    }

    public List<SolarYearlyChart> getSolarYearlyCharts() {
        return solarYearlyCharts;
    }

    public void setSolarYearlyCharts(List<SolarYearlyChart> radarYearlyCharts) {
        this.solarYearlyCharts = radarYearlyCharts;
    }


    public static class SolarYearlyChart {
        private int year;
        private String groupId; // indexName (chaque index aura sa propre list de label et de solarArea
        private List<String> indexLabels; // the index names
        private List<SolarArea> solarAreas;

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

        public List<SolarArea> getSolarAreas() {
            return solarAreas;
        }

        public void setSolarAreas(List<SolarArea> solarAreas) {
            this.solarAreas = solarAreas;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
    }

    public static class SolarArea {
        private String areaName;
        private String backgroundColor; // rgba(255,99,132,1);
        private RichValue data;

        public RichValue getData() {
            return data;
        }

        public void setData(RichValue data) {
            this.data = data;
        }

        public String getAreaName() {
            return areaName;
        }

        public void setAreaName(String areaName) {
            this.areaName = areaName;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }
    }
}
