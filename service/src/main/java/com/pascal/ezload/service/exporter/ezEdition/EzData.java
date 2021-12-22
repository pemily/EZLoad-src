package com.pascal.ezload.service.exporter.ezEdition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pascal.ezload.service.model.EZDate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.pascal.ezload.service.util.ModelUtils.str2Float;
import static com.pascal.ezload.service.util.ModelUtils.str2Int;

public class EzData {

    private Map<String, String> data = new HashMap<>();

    public EzData(){
    }

    public EzData(EzData data) {
        this.data = new HashMap<>(data.data);
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
    public void put(EzDataKey key, String value){
        String val = value == null ? "" : value;
        if (containsKey(key.getName()) && !Objects.equals(val, get(key)))
            throw new RuntimeException("There is already a variable with this key: "+key+" current Value:"
                    +get(key)+" new Value: "+val);
        this.data.put(key.getName(), val);
    }

    @JsonIgnore
    public String get(String name) {
        return this.data.get(name);
    }

    @JsonIgnore
    public String get(EzDataKey k) {
        return this.data.get(k.getName());
    }

    @JsonIgnore
    public int getInt(EzDataKey k){
        return str2Int(get(k));
    }

    @JsonIgnore
    public float getFloat(EzDataKey k){
        return str2Float(get(k));
    }

    @JsonIgnore
    public EZDate getDate(EzDataKey k) {
        String date = get(k);
        if (date == null) return null;
        return EZDate.parseFrenchDate(date, '/');
    }

    @JsonIgnore
    public boolean containsKey(String name) {
        return this.data.containsKey(name);
    }

    public String generateId(){
        return this.data.hashCode()+"";
    }
}
