package com.pascal.ezload.service.dashboard;

import java.util.List;

public class ChartLine {

    private String title;
    private List<Float> values;
    private String colorLine; // rgba(255,99,132,1);

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
}
