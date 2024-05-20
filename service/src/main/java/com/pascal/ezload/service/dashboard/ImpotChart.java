package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.dashboard.config.ImpotChartSettings;

import java.util.LinkedList;
import java.util.List;

public class ImpotChart extends ImpotChartSettings {

    private List<ImpotAnnuel> impotAnnuels = new LinkedList<>();

    public ImpotChart(){}
    public ImpotChart(ImpotChartSettings chartSettings) {
        this.setTitle(chartSettings.getTitle());
        this.setEzPortfolioDeviseCode(chartSettings.getEzPortfolioDeviseCode());
    }

    public List<ImpotAnnuel> getImpotAnnuels() {
        return impotAnnuels;
    }

    public void setImpotAnnuels(List<ImpotAnnuel> impotAnnuels) {
        this.impotAnnuels = impotAnnuels;
    }

    public static class ImpotAnnuel {
        private int year;
        private String declaration;

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getDeclaration() {
            return declaration;
        }

        public void setDeclaration(String declaration) {
            this.declaration = declaration;
        }
    }
}
