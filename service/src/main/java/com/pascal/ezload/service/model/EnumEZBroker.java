package com.pascal.ezload.service.model;

public enum EnumEZBroker {
    BourseDirect("Bourse Direct", "BourseDirect");

    private String ezPortfolioName, dirName;

    EnumEZBroker(String ezPortfolioName, String dirName){
        this.ezPortfolioName = ezPortfolioName;
        this.dirName = dirName;
    }

    public String getEzPortfolioName(){
        return ezPortfolioName;
    }

    public String getDirName() {
        return dirName;
    }
}
