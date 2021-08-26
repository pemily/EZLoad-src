package com.pascal.bientotrentier.service.gdrive;

import java.util.List;

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

}
