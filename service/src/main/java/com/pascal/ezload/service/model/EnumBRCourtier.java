package com.pascal.ezload.service.model;

public enum EnumBRCourtier {
    BourseDirect("Bourse Direct", "BourseDirect");

    private String displayName, dirName;

    EnumBRCourtier(String displayName, String dirName){
        this.displayName = displayName;
        this.dirName = dirName;
    }

    public String getEzPortfolioName(){
        return displayName;
    }

    public String getDirName() {
        return dirName;
    }
}
