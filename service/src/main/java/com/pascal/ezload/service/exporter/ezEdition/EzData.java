package com.pascal.ezload.service.exporter.ezEdition;

import java.util.HashMap;
import java.util.Map;

public class EzData {

    private Map<String, String> data = new HashMap<>();

    public EzData(){
    }

    public EzData(EzData data) {
        try {
            this.data = (Map<String, String>) data.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public EzData(Map<String, String> data){
        this.data = data;
    }


    public void put(String key, String value){
        this.data.put(key, value);
    }

    public String get(String name) {
        return this.data.get(name);
    }

    public boolean containsKey(String name) {
        return this.data.containsKey(name);
    }
}
