package com.pascal.bientotrentier.model;

public enum EnumBRCourtier {
    BourseDirect("BourseDirect");

    private String displayName;

    EnumBRCourtier(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}
