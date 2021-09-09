package com.pascal.ezload.service.sources.bourseDirect;

import com.pascal.ezload.service.model.BRAccountDeclaration;

public class BourseDirectBRAccountDeclaration implements BRAccountDeclaration {

    private String name;
    private String number;
    private boolean active;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
