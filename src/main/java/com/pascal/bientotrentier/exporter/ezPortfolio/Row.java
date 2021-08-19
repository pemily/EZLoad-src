package com.pascal.bientotrentier.exporter.ezPortfolio;

import java.util.List;

public class Row {

    private List<Object> row;

    public Row(List<Object> row){
        this.row = row;
    }

    public Object value(int colIndex){
        return colIndex >= row.size() ? null : row.get(colIndex);
    }
}
