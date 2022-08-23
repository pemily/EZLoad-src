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

import com.pascal.ezload.service.model.EZDate;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.pascal.ezload.service.util.NumberUtils.str2Float;
import static com.pascal.ezload.service.util.NumberUtils.str2Int;

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
        EZDate r = EZDate.parseFrenchDate(date, '/');
        if (r == null){
            try {
                LocalDate localDate = LocalDate.of(1899, Month.DECEMBER, 30).plusDays(str2Int(date));
                return new EZDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
            }
            catch(Exception e){
                r = null;
            }
        }
        return r;
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
