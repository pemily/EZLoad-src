package com.pascal.ezload.service.dashboard.config;

import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.DeviseUtil;

public class ImpotChartSettings extends Checkable<ImpotChartSettings> implements DashboardChart  {

    private String title;
    private float buyIncreasePercent = 15; // mettre 15 == 15% pour IBM pour les stocks options et 0 pour les autres comptes
    private String ezPortfolioDeviseCode = DeviseUtil.USD.getCode(); // pour IBM mettre $, sinon euro

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

    public float getBuyIncreasePercent() {
        return buyIncreasePercent;
    }

    public void setBuyIncreasePercent(float buyIncreasePercent) {
        this.buyIncreasePercent = buyIncreasePercent;
    }

    public String getEzPortfolioDeviseCode() {
        return ezPortfolioDeviseCode;
    }

    public void setEzPortfolioDeviseCode(String ezPortfolioDeviseCode) {
        this.ezPortfolioDeviseCode = ezPortfolioDeviseCode;
    }
}
