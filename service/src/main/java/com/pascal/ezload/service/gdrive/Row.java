package com.pascal.ezload.service.gdrive;

import static com.pascal.ezload.service.util.ModelUtils.str2Float;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pascal.ezload.service.model.EZDate;

public class Row {

    private final List<Object> values;

    public Row(List<Object> values){
        this.values = values;
    }

    public Row(String... values){
        this.values = Arrays.asList(values);
    }

    public Row(int nbOfCol){
        this.values = new ArrayList<>(nbOfCol);
        for (int i = 0; i < nbOfCol; i++)
            this.values.add(i, "");
    }

    public EZDate valueDate(int colIndex) {
        String date = getValueStr(colIndex);
        if (date == null) return null;
        return EZDate.parseFrenchDate(date, '/');
    }

    public List<Object> getValues() {
        return values;
    }

    public String getValueStr(int colIndex){
        return colIndex >= values.size() ? null : (String) values.get(colIndex);
    }

    public float getValueFloat(int colIndex) {
        if(colIndex >= values.size()) throw new IllegalStateException(values.size()+" < "+colIndex);
        return str2Float((String)values.get(colIndex));
    }

    public void setValue(int colIndex, String s) {
        values.set(colIndex, s);
    }

    public void setValue(int colIndex, EZDate d) {
        values.set(colIndex, d.toEzPortoflioDate());
    }
}
