package com.pascal.ezload.service.util;

import java.io.File;

public class FileValue {

    private final boolean required;

    public FileValue(boolean required){        
        this.required = required;
    }


    public void validate(Checkable checkable, String field, String value){
        if (value != null && new File(value).exists()){
            if (!new File(value).isFile())
                checkable.setErrorMsg(field, "Cette valeur ne représente pas un fichier");
            else checkable.setErrorMsg(field, null);
        }
        else{
            if (required)
                checkable.setErrorMsg(field, "Le fichier n'existe pas");
            else checkable.setErrorMsg(field, null);
        }
    }

}