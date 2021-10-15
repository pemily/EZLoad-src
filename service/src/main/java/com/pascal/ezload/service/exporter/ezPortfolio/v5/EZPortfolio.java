package com.pascal.ezload.service.exporter.ezPortfolio.v5;

public class EZPortfolio {
    private MesOperations mesOperations;
    private MonPortefeuille monPortefeuille;
    private final String ezPortfolioVersion;

    public EZPortfolio(String ezPortfolioVersion){
        this.ezPortfolioVersion = ezPortfolioVersion;
    }

    public MesOperations getMesOperations() {
        return mesOperations;
    }

    public void setMesOperations(MesOperations mesOperations) {
        this.mesOperations = mesOperations;
    }

    public MonPortefeuille getMonPortefeuille() {
        return monPortefeuille;
    }

    public void setMonPortefeuille(MonPortefeuille monPortefeuille) {
        this.monPortefeuille = monPortefeuille;
    }

    public String getEzPortfolioVersion() {
        return ezPortfolioVersion;
    }

}
