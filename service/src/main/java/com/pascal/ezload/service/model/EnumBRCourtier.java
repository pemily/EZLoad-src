package com.pascal.ezload.service.model;

public enum EnumBRCourtier {
    BourseDirect("Bourse Direct", "BourseDirect");

    private String ezPortfolioName, dirName;

    EnumBRCourtier(String ezPortfolioName, String dirName){
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
