package com.pascal.bientotrentier.service.gdrive;

import com.pascal.bientotrentier.service.model.BRDate;
import com.pascal.bientotrentier.service.util.ModelUtils;

import java.util.Arrays;
import java.util.List;

import static com.pascal.bientotrentier.service.util.ModelUtils.str2Float;

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
