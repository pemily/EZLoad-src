package com.pascal.ezload.service.gdrive;

import static com.pascal.ezload.service.util.ModelUtils.str2Float;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.pascal.ezload.service.model.EZDate;
import org.apache.commons.lang3.StringUtils;

public class Row {

    private final List<Object> values;

    public Row(){
        this.values = new LinkedList<>();
    }

    public Row(List<Object> values){
        this.values = values;
    }

    public EZDate getValueDate(int colIndex) {
        String date = getValueStr(colIndex);
        if (StringUtils.isBlank(date)) return null;
        return EZDate.parseFrenchDate(date, '/');
    }

    public List<Object> getValues() {
        return values;
    }

    public String getValueStr(int colIndex){
        return colIndex >= values.size() ? "" : (String) values.get(colIndex);
    }

    public float getValueFloat(int colIndex) {
        if(colIndex >= values.size()) throw new IllegalStateException(values.size()+" < "+colIndex);
        return str2Float((String)values.get(colIndex));
    }

    public void setValue(int colIndex, String s) {
        if (values.size() <= colIndex) {
            for (int i = values.size(); i <= colIndex; i++)
                this.values.add("");
        }
        values.set(colIndex, s == null ? "" : s);
    }

    public void setValue(int colIndex, EZDate d) {
        values.set(colIndex, d.toEzPortoflioDate());
    }

    public Row createDeepCopy(){
        return new Row(values.stream().map(v -> {
                if (v instanceof String) return v;
                else throw new IllegalStateException("Do I have another type in the Row??"); // I think I have only Strings
            }).collect(Collectors.toList()));
    }
}
