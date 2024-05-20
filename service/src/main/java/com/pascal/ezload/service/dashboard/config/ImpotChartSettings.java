package com.pascal.ezload.service.dashboard.config;

import com.pascal.ezload.service.util.Checkable;

public class ImpotChartSettings extends Checkable<ImpotChartSettings> implements DashboardChart  {

    private String title;
    private String ezPortfolioDeviseCode;
    private String urlPlusMoinsValueReportable;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public ChartType type() {
        return ChartType.IMPOT;
    }

    @Override
    public ImpotChartSettings validate() {
        return this;
    }

    @Override
    public void clearErrors() {

    }

    public String getEzPortfolioDeviseCode() {
        return ezPortfolioDeviseCode;
    }

    public void setEzPortfolioDeviseCode(String ezPortfolioDeviseCode) {
        this.ezPortfolioDeviseCode = ezPortfolioDeviseCode;
    }

    public String getUrlPlusMoinsValueReportable() {
        return urlPlusMoinsValueReportable;
    }

    public void setUrlPlusMoinsValueReportable(String urlPlusMoinsValueReportable) {
        this.urlPlusMoinsValueReportable = urlPlusMoinsValueReportable;
    }
}
