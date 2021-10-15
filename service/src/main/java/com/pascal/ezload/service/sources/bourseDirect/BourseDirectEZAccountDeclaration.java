package com.pascal.ezload.service.sources.bourseDirect;

import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.StringValue;

public class BourseDirectEZAccountDeclaration extends Checkable implements EZAccountDeclaration {

    public enum Field{name, number}

    private String name = null;
    private String number = null;
    private boolean active;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number == null ? null : number.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void validate(){
        new StringValue(true).validate(this, Field.name.name(), name);
        new StringValue(true).validate(this, Field.number.name(), number);
    }

}
