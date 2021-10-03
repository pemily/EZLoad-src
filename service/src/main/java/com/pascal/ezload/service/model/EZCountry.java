package com.pascal.ezload.service.model;

public class EZCountry {
    private String code;
    private String name;

    public EZCountry(String code, String name) {
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
