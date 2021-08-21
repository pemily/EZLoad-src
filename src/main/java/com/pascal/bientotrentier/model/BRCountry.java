package com.pascal.bientotrentier.model;

public class BRCountry {
    private String code;
    private String name;

    public BRCountry(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
