package com.pascal.ezload.service.exporter.ezEdition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pascal.ezload.service.model.EZDate;

import java.util.HashMap;
import java.util.Map;

import static com.pascal.ezload.service.util.ModelUtils.str2Float;
import static com.pascal.ezload.service.util.ModelUtils.str2Int;

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

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @JsonIgnore
    public void put(String key, String value){
        if (containsKey(key)) throw new RuntimeException("Il y a déjà une variable dans la liste avec le même nom. Sa valeur: "+this.data.get(key));
        this.data.put(key, value);
    }

    @JsonIgnore
    public String get(String name) {
        return this.data.get(name);
    }

    @JsonIgnore
    public int getInt(String name){
        return str2Int(get(name));
    }

    @JsonIgnore
    public float getFloat(String name){
        return str2Float(get(name));
    }

    @JsonIgnore
    public EZDate getDate(String name) {
        String date = get(name);
        if (date == null) return null;
        return EZDate.parseFrenchDate(date, '/');
    }

    @JsonIgnore
    public boolean containsKey(String name) {
        return this.data.containsKey(name);
    }
}
