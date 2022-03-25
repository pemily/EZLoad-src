package com.pascal.ezload.service.gdrive;

import static com.pascal.ezload.service.util.ModelUtils.str2Float;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.pascal.ezload.service.model.EZDate;
import org.apache.commons.lang3.StringUtils;

public class Row {

    private final List<Object> values;
    private int rowNumber;

    public Row(int rowNumber){
        this.values = new LinkedList<>();
        this.rowNumber = rowNumber;
    }

    public Row(int rowNumber, List<Object> values){
        this.values = values;
        this.rowNumber = rowNumber;
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
        return colIndex >= values.size() ? "" : values.get(colIndex).toString();
    }

    public float getValueFloat(int colIndex) {
        if(colIndex >= values.size()) throw new IllegalStateException(values.size()+" < "+colIndex);
        return str2Float(values.get(colIndex).toString());
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
        return new Row(rowNumber, values.stream().map(v -> {
                if (v instanceof String) return v;
                if (v instanceof BigDecimal) return v;
                else throw new IllegalStateException("Do I have another type in the Row?? "+v.getClass()+" content: "+v); // I thought I have only Strings
            }).collect(Collectors.toList()));
    }

    public int getRowNumber(){
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }
}
