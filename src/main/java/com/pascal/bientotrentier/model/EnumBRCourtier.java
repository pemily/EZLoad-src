package com.pascal.bientotrentier.model;

public enum EnumBRCourtier {
    BourseDirect("Bourse Direct");

    private String displayName;

    EnumBRCourtier(String displayName){
        this.displayName = displayName;
    }

    public String getEzPortfolioName(){
        return displayName;
    }
}
