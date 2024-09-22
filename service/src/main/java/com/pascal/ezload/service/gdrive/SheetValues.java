/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.gdrive;

import com.pascal.ezload.common.util.StringUtils;

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

    public CellXY[] getCellsRange(){
        return extractCells(range);
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
        if (cells == null){
            cells = new String[]{r[1], r[1]}; // no : to separate, the range correspond to only one cell
        }
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

    public static class CellXY {
        CellXY(String colLetter, int rowNumber){
            this.colLetter = colLetter;
            this.rowNumber = rowNumber;
        }

        String colLetter;
        int rowNumber; // -1 if the Row is not specified => example: when the Range is just MesOperations!A1:L <= Toutes les lignes avec des données colonnes: A-L

        public String getColLetter() {
            return colLetter;
        }

        public int getRowNumber() {
            return rowNumber;
        }
    }
}
