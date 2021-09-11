package com.pascal.ezload.service.gdrive;

import static com.pascal.ezload.service.util.ModelUtils.str2Float;

import java.util.Arrays;
import java.util.List;

import com.pascal.ezload.service.model.BRDate;

public class Row {

    private List<Object> values;

    public Row(List<Object> values){
        this.values = values;
    }

    public Row(String... values){
        this.values = Arrays.asList(values);
    }


    public BRDate valueDate(int colIndex) {
        String date = valueStr(colIndex);
        if (date == null) return null;
        return BRDate.parseFrenchDate(date, '/');
    }

    public List<Object> getValues() {
        return values;
    }

    public String valueStr(int colIndex){
        return colIndex >= values.size() ? null : (String) values.get(colIndex);
    }

    public float valueFloat(int colIndex) {
        return colIndex >= values.size() ? null : str2Float((String)values.get(colIndex));
    }

    public void setValue(int colIndex, String s) {
        values.set(colIndex, s);
    }

    public void setValue(int colIndex, BRDate d) {
        values.set(colIndex, d.toEzPortoflioDate());
    }
}
