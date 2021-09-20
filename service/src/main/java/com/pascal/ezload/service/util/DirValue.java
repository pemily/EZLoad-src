package com.pascal.ezload.service.util;

import java.io.File;

public class DirValue {

    private final boolean required;

    public DirValue(boolean required){        
        this.required = required;
    }   

    public void validate(Checkable checkable, String field, String value){
        if (value != null && new File(value).exists()){
            if (!new File(value).isDirectory())
                checkable.setErrorMsg(field, "Cette valeur ne représente pas un repertoire");
            else checkable.setErrorMsg(field, null);
        }
        else{
            if (required)
                checkable.setErrorMsg(field, "Le repertoire n'existe pas");
            else checkable.setErrorMsg(field, null);
        }
    }

}