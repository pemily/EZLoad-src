package com.pascal.ezload.service.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;


public class StringValue {

    private final boolean required;

    public StringValue(boolean required){
        this.required = required;
    }

    public void validate(Checkable checkable, String field, String value){
        if (required && StringUtils.isBlank(value)){
            checkable.setErrorMsg(field, "Cette valeur ne doit pas être vide");
        }
        else checkable.setErrorMsg(field, null);
    }

    public void validate(Checkable checkable, String field, String[] values){
        if (required && Arrays.stream(values).allMatch(StringUtils::isBlank)){
            checkable.setErrorMsg(field, "Cette valeur ne doit pas être vide");
        }
        else checkable.setErrorMsg(field, null);
    }
}