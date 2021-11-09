package com.pascal.ezload.service.gdrive;

import java.util.List;
import java.util.stream.Collectors;

public class SheetValues {
    private String range;
    private List<Row> values;

    public SheetValues(String range, List<Row> values){
        this.range = range;
        this.values = values;
    }

    public String getRange() {
        return range;
    }

    public List<Row> getValues() {
        return values;
    }

    public SheetValues createDeepCopy() {
        return new SheetValues(range, values.stream().map(Row::createDeepCopy).collect(Collectors.toList()));
    }
}
