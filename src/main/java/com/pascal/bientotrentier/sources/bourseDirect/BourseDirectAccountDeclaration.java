package com.pascal.bientotrentier.sources.bourseDirect;

import com.pascal.bientotrentier.MainSettings;

public class BourseDirectAccountDeclaration implements MainSettings.AccountDeclaration {

    private String number;
    private String name;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
