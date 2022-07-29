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
package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.EZAction;

import java.util.*;
import java.util.stream.Collectors;

public class EZLoadShareSheet {

    private static final int ISIN_COL = 0;
    private static final int TICKER_COL = 1;
    private static final int SHARE_NAME_COL = 2;
    private static final int SHARE_TYPE_COL = 3;
    private static final int SHARE_COUNTRY_CODE_COL = 4;

    public static final String ISIN_HEADER = "ISIN";

    private final List<Object> headers = Arrays.asList(ISIN_HEADER, "Ticker", "Valeur", "Type", "Code Pays");

    private final SheetValues existingShareValues;
    private final List<Row> newShareValues = new ArrayList<>();

    public EZLoadShareSheet(SheetValues existingShareValues) {
        this.existingShareValues = existingShareValues;
    }

    public void addHeaderIfMissing() {
        if (existingShareValues.getValues().size() == 0 && newShareValues.size() == 0){
            newShareValues.add(new Row(1, headers));
        }
    }

    public int getNbOfExistingShareValues(){
        return existingShareValues.getValues().size();
    }

    public List<Row> getNewShareValues(){
        return newShareValues;
    }


    public void newShareValue(EZAction newShareValue) {
        if (!exists(newShareValue)) {
            Row r = new Row(EZPorfolioProxyV5.FIRST_ROW_EZLOAD_SHARES + existingShareValues.getValues().size() + newShareValues.size());
            r.setValue(ISIN_COL, newShareValue.getIsin());
            r.setValue(TICKER_COL, newShareValue.getEzTicker());
            r.setValue(SHARE_NAME_COL, newShareValue.getEzName());
            r.setValue(SHARE_TYPE_COL, newShareValue.getType());
            r.setValue(SHARE_COUNTRY_CODE_COL, newShareValue.getCountryCode());
            newShareValues.add(r);
        }
    }


    public EZLoadShareSheet createDeepCopy() {
        EZLoadShareSheet copy = new EZLoadShareSheet(existingShareValues.createDeepCopy());
        copy.newShareValues.addAll(newShareValues.stream().map(Row::createDeepCopy).collect(Collectors.toList()));
        return copy;
    }

    public boolean exists(EZAction shareValue) {
        return existingShareValues.getValues()
                .stream()
                .anyMatch(r -> Objects.equals(r.getValueStr(ISIN_COL), shareValue.getIsin()))
                ||
                newShareValues
                        .stream()
                        .anyMatch(r -> Objects.equals(r.getValueStr(ISIN_COL), shareValue.getIsin()));
    }

    public Optional<EZAction> findByIsin(String isin){
        return existingShareValues.getValues()
                .stream()
                .filter(s -> Objects.equals(s.getValueStr(ISIN_COL), isin))
                .findFirst()
                .or(() -> newShareValues
                        .stream()
                        .filter(s -> Objects.equals(s.getValueStr(ISIN_COL), isin))
                        .findFirst())
                .map(EZLoadShareSheet::row2EZAction);
    }

    public static EZAction row2EZAction(Row r) {
        return new EZAction(r.getValueStr(ISIN_COL),
                r.getValueStr(TICKER_COL),
                r.getValueStr(SHARE_NAME_COL),
                r.getValueStr(SHARE_TYPE_COL),
                r.getValueStr(SHARE_COUNTRY_CODE_COL));
    }

    public static boolean isNotHeader(Row row) {
        return !Objects.equals(row.getValueStr(ISIN_COL), ISIN_HEADER);
    }

    public void updateNewShareValue(EZAction shareValue) {
        newShareValues
                .stream()
                .filter(sv -> sv.getValueStr(ISIN_COL).equals(shareValue.getIsin()))
                .forEach(sv -> {
                    sv.setValue(TICKER_COL, shareValue.getEzTicker());
                    sv.setValue(SHARE_NAME_COL, shareValue.getEzName());
                    sv.setValue(SHARE_TYPE_COL, shareValue.getType());
                    sv.setValue(SHARE_COUNTRY_CODE_COL, shareValue.getCountryCode());
                });
    }
}
