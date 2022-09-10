package com.pascal.ezload.service.dashboard;

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