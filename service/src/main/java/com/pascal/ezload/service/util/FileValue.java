package com.pascal.ezload.service.util;

import java.io.File;

public class FileValue {

    private final Checkable<?> checkable;
    private final String field;
    private final String value;

    public FileValue(Checkable<?> checkable, String field, String value){
        this.checkable = checkable;
        this.field = field;
        this.value = value;
    }

    public FileValue checkRequired(){
        if (value == null || !new File(value).exists()){
            checkable.setErrorMsg(field, "Le repertoire n'existe pas");
        }
        return this;
    }

    public FileValue checkDirectory(){
        if (value != null && new File(value).exists()){
            if (!new File(value).isDirectory())
                checkable.setErrorMsg(field, "Cette valeur ne représente pas un repertoire");
        }
        return this;
    }

    public FileValue checkFile(){
        if (value != null && new File(value).exists()){
            if (!new File(value).isFile())
                checkable.setErrorMsg(field, "Cette valeur ne représente pas un fichier");
        }
        return this;
    }

}