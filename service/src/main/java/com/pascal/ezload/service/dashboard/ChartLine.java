package com.pascal.ezload.service.dashboard;

import java.util.List;

public class ChartLine {


    public enum LineStyle {
        LINE_WITH_LEGENT_AT_LEFT("yAxisLeft"),
        LINE_WITH_LEGENT_AT_RIGHT("yAxisRight"),
        PERF_LINE("yAxisPerf"),
        BAR("bar");

        private String style;

        LineStyle(String style) {
            this.style = style;
        }

        String getStyleName() {
            return style;
        }
    }

    private String title;
    private List<Float> values;
    private String colorLine; // rgba(255,99,132,1);
    private LineStyle lineStyle; // optionel, pour ajouter une autre echelle sur l'axe des Y (pour faire la distinction entre action/devise par exemple)

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Float> getValues() {
        return values;
    }

    public void setValues(List<Float> values) {
        this.values = values;
    }

    public String getColorLine() {
        return colorLine;
    }

    public void setColorLine(String colorLine) {
        this.colorLine = colorLine;
    }

    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(LineStyle lineStyle) {
        this.lineStyle = lineStyle;
    }

    public String toString(){
        return title;
    }
}