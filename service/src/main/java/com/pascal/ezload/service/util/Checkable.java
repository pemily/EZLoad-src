package com.pascal.ezload.service.util;

import java.util.HashMap;
import java.util.Map;

public abstract class Checkable {

    private Map<String, String> field2ErrorMsg = new HashMap<>();

    public Map<String, String> getField2ErrorMsg() {
        return field2ErrorMsg;
    }

    public void setField2ErrorMsg(Map<String, String> field2ErrorMsg) {
        this.field2ErrorMsg = field2ErrorMsg;
    }

    public abstract void validate();

    public String getErrorMsg(String fieldName){
        if (this.getField2ErrorMsg() == null) return null;
        return this.getField2ErrorMsg().get(fieldName);
    }

    public void setErrorMsg(String fieldName, String errorMsg){
        if (this.getField2ErrorMsg() == null) setField2ErrorMsg(new HashMap<>());
        this.getField2ErrorMsg().put(fieldName, errorMsg);
    }

    public void clearErrors(){
        setField2ErrorMsg(null);
    }

    public boolean hasError(){
        return field2ErrorMsg != null && !field2ErrorMsg.isEmpty();
    }
}
