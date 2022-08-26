package com.pascal.ezload.service.dashboard;

import java.util.List;

public class Chart {

    private String mainTitle, TitleX, TitleY;
    private List<Object> labels;
    private List<ChartLine> lines;

    public List<Object> getLabels() {
        return labels;
    }

    public void setLabels(List<Object> labels) {
        this.labels = labels;
    }

    public List<ChartLine> getLines() {
        return lines;
    }

    public void setValues(List<ChartLine> lines) {
        this.lines = lines;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }


    public String getTitleX() {
        return TitleX;
    }

    public void setTitleX(String titleX) {
        TitleX = titleX;
    }

    public String getTitleY() {
        return TitleY;
    }

    public void setTitleY(String titleY) {
        TitleY = titleY;
    }
}
