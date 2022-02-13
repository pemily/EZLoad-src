package com.pascal.ezload.service.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;


public class StringValue {
    private final Checkable<?> checkable;
    private final String field;
    private final String value;

    public StringValue(Checkable<?> checkable, String field, String value){
        this.checkable = checkable;
        this.field = field;
        this.value = value;
    }

    public StringValue checkRequired(){
        if (StringUtils.isBlank(value)){
            checkable.setErrorMsg(field, "Cette valeur ne doit pas être vide");
        }
        return this;
    }

    public StringValue validateWithForbidenValues(String... forbidenValues){
        if (Arrays.stream(forbidenValues).anyMatch(forbidenValue ->  forbidenValue.equals(value))){
            checkable.setErrorMsg(field, "Cette valeur est interdite");
        }
        return this;
    }


    public void checkPrefixMatch(String matchPrefix){
        if (matchPrefix == null){
            if (!value.startsWith("https://"))
                checkable.setErrorMsg(field, "Cette valeur doit commencer par https://");
            else if (value.equals("https://"))
                checkable.setErrorMsg(field, "Cette valeur est incomplète");
            else
                checkable.setErrorMsg(field, null);
        }
        else {
            if (!value.startsWith(matchPrefix))
                checkable.setErrorMsg(field, "Cette valeur doit commencer par " + matchPrefix);
            else if (value.equals(matchPrefix))
                checkable.setErrorMsg(field, "Cette valeur est incomplète");
            else
                checkable.setErrorMsg(field, null);
        }
    }

}