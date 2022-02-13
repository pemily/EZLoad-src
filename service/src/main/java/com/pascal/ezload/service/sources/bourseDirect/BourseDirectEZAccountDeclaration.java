package com.pascal.ezload.service.sources.bourseDirect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.AccountData;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.StringValue;

public class BourseDirectEZAccountDeclaration extends Checkable<BourseDirectEZAccountDeclaration> implements EZAccountDeclaration, AccountData {

    public enum Field{name, number}

    private String name = null;
    private String number = null;
    private boolean active;

    public String getNumber() {
        return number;
    }

    @Override
    @JsonIgnore
    public EnumEZBroker getEzBroker() {
        return EnumEZBroker.BourseDirect;
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

    @Override
    public void fill(EzData data) {
        data.put(account_name, name);
        data.put(account_number, number);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BourseDirectEZAccountDeclaration validate(){
        new StringValue(this, Field.name.name(), name).checkRequired();
        new StringValue(this, Field.number.name(), number).checkRequired();
        return this;
    }

}
