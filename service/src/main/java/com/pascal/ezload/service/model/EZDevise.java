package com.pascal.ezload.service.model;

public class EZDevise {
    private String symbol; // $
    private String code; // USD

    public EZDevise(String code, String symbol){
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
