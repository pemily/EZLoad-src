package com.pascal.bientotrentier.gdrive;

import com.pascal.bientotrentier.util.ModelUtils;

import java.util.Arrays;
import java.util.List;

import static com.pascal.bientotrentier.util.ModelUtils.str2Float;

public class Row {

    private List<Object> values;

    public Row(List<Object> values){
        this.values = values;
    }

    public Row(Object... values){
        this.values = Arrays.asList(values);
    }
/*
    public Object value(int colIndex){
        return colIndex >= values.size() ? null : values.get(colIndex);
    }*/

    public String valueStr(int colIndex){
        return colIndex >= values.size() ? null : (String) values.get(colIndex);
    }

    public List<Object> getValues() {
        return values;
    }

    public float valueFloat(int colIndex) {
        return colIndex >= values.size() ? null : str2Float((String)values.get(colIndex));
    }

    public void setValue(int colIndex, String s) {
        values.set(colIndex, s);
    }
}
