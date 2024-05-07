package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.dashboard.config.ImpotChartSettings;

import java.util.LinkedList;
import java.util.List;

public class ImpotChart extends ImpotChartSettings {

    private List<String> generatedFiles = new LinkedList<>();

    public ImpotChart(ImpotChartSettings chartSettings) {
        this.setTitle(chartSettings.getTitle());
    }

    public List<String> getGeneratedFiles() {
        return generatedFiles;
    }

    public void setGeneratedFiles(List<String> generatedFiles) {
        this.generatedFiles = generatedFiles;
    }
}
