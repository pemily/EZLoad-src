package com.pascal.ezload.service.model;

public class BRDevise {
    private String symbol; // $
    private String code; // USD

    public BRDevise(String code, String symbol){
        this.code = code;
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }


    public String getCode() {
        return code;
    }

}
