package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.dashboard.config.SolarChartSettings;

import java.util.List;

public class SolarChart extends SolarChartSettings {

    private List<SolarYearlyChart> solarYearlyCharts;

    public SolarChart(){}
    public SolarChart(SolarChartSettings chartSettings) {
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
        private String groupId; // indexName (chaque index aura sa propre list de label et de solarArea)
        private List<String> indexLabels; // the labels
        private List<SolarArea> solarAreas;
        private ChartLine.Y_AxisSetting yAxisSetting;
        private String yAxisTitle;

        public ChartLine.Y_AxisSetting getYAxisSetting() {
            return yAxisSetting;
        }

        public void setYAxisSetting(ChartLine.Y_AxisSetting yAxisSetting) {
            this.yAxisSetting = yAxisSetting;
        }


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

        public String getYAxisTitle() {
            return yAxisTitle;
        }

        public void setYAxisTitle(String yAxisTitle) {
            this.yAxisTitle = yAxisTitle;
        }
    }

    public static class SolarArea {
        private String backgroundColor; // rgba(255,99,132,1);
        private RichValue data;

        public RichValue getData() {
            return data;
        }

        public void setData(RichValue data) {
            this.data = data;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }
    }
}
