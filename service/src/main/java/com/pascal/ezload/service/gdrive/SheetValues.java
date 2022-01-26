package com.pascal.ezload.service.gdrive;

import com.pascal.ezload.service.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SheetValues {
    private String range;
    private List<Row> values;

    private SheetValues(String range, List<Row> values){
        this.range = range;
        this.values = values;
    }

    public static SheetValues createFromObjectLists(String range, List<List<Object>> values){
        CellXY[] cellsRange = extractCells(range);
        AtomicInteger cpt = new AtomicInteger(cellsRange[0].rowNumber);
        List<Row> rows = values == null ? new LinkedList<>() :
                values.stream().map(v -> new Row(cpt.getAndIncrement(), v)).collect(Collectors.toList());
        return new SheetValues(range, rows);
    }

    public static SheetValues createFromRowLists(String range, List<Row> values){
        CellXY[] cellsRange = extractCells(range);
        AtomicInteger cpt = new AtomicInteger(cellsRange[0].rowNumber);
        List<Row> rows = values == null ? new LinkedList<>() :
                values.stream().map(r -> {
                    Row rClone = r.createDeepCopy();
                    rClone.setRowNumber(cpt.getAndIncrement()); // si les 2 nombres sont different, la row est migré vers une autre destination
                    return rClone;
                }).collect(Collectors.toList());

        return new SheetValues(range, rows);
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


    // range looks like: MesOperations!D1:D1
    private static CellXY[] extractCells(String range){
        String r[] = StringUtils.divide(range, '!');
        assert r != null;
        String cells[] = StringUtils.divide(r[1], ":");
        CellXY start = extractCell(cells[0]);
        CellXY end = extractCell(cells[1]);
        return new CellXY[]{ start, end };
    }

    // ex. cellCoordinate: D12 | AB12
    private static CellXY extractCell(String cellCoordinate){
        StringBuilder col = new StringBuilder();
        StringBuilder row = new StringBuilder();
        for (char c : cellCoordinate.toCharArray()){
            if (c >= 'A' && c <= 'Z'){
                col.append(c);
            }
            else{
                row.append(c);
            }
        }

        int rowNumber = -1;
        if (!row.toString().equals("")) rowNumber = Integer.parseInt(row.toString());
        return new CellXY(col.toString(), rowNumber);
    }

    private static class CellXY {
        CellXY(String colLetter, int rowNumber){
            this.colLetter = colLetter;
            this.rowNumber = rowNumber;
        }

        String colLetter;
        int rowNumber; // -1 if the Row is not specified => example: when the Range is just MesOperations!A1:L <= Toutes les lignes avec des données colonnes: A-L
    }
}
