package com.pascal.ezload.service.util;

import org.apache.commons.lang3.StringUtils;

public class HttpValue {

    private final boolean required;
    private final String matchPrefix;

    public HttpValue(boolean required, String matchPrefix){
        this.required = required; this.matchPrefix = matchPrefix;
    }


    public void validate(Checkable checkable, String field, String value){
        if (StringUtils.isBlank(value)){
            if (required) checkable.setErrorMsg(field, "Cette valeur ne doit pas être vide");
            else checkable.setErrorMsg(field, null);
        }
        else{
            if (matchPrefix == null){
                if (!value.startsWith("https://"))
                   checkable.setErrorMsg(field, "Cette valeur doit commencer par https://");
                else if (value.equals("https://"))
                    checkable.setErrorMsg(field, "Cette valeur est incomplète");
                else
                    checkable.setErrorMsg(field, null);
            }
            else {
                if (matchPrefix != null && !value.startsWith(matchPrefix))
                    checkable.setErrorMsg(field, "Cette valeur doit commencer par " + matchPrefix);
                else if (value.equals(matchPrefix))
                    checkable.setErrorMsg(field, "Cette valeur est incomplète");
                else
                    checkable.setErrorMsg(field, null);
            }

        }
    }

}