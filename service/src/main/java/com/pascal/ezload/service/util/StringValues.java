package com.pascal.ezload.service.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;


public class StringValues {
    private final Checkable<?> checkable;
    private final String field;
    private final String[] values;

    public StringValues(Checkable<?> checkable, String field, String[] values){
        this.checkable = checkable;
        this.field = field;
        this.values = values;
    }

    public StringValues checkRequired(){
        if (Arrays.stream(values).allMatch(StringUtils::isBlank)){
            checkable.setErrorMsg(field, "Cette valeur ne doit pas être vide");
        }
        return this;
    }

}