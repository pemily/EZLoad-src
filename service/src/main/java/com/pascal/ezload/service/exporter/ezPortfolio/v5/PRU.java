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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PRU {
    private static final int SHARE_NAME = 0;

    private final SheetValues existingPRUs;
    private final List<Row> newPRUs = new ArrayList<>();

    public PRU(SheetValues existingPRUs) {
        this.existingPRUs = existingPRUs;
    }


    public String getPRUCellReference(String ezShareName){
        int refIndex = EZPorfolioProxyV5.FIRST_ROW_PRU; // the first row start in ezPortfolio
        boolean found = false;
        for (Row row : existingPRUs.getValues()){
            if (row.getValueStr(SHARE_NAME).equals(ezShareName)){
                found = true;
                break;
            }
            refIndex++;
        }
        if (found) return "PRU!B"+refIndex;
        for (Row row : newPRUs) {
            if (row.getValueStr(SHARE_NAME).equals(ezShareName)) {
                found = true;
                break;
            }
            refIndex++;
        }
        if (found) return "PRU!B"+refIndex;
        return null;
    }


    public void newPRU(String ezShareName){
        Row r = new Row(EZPorfolioProxyV5.FIRST_ROW_PRU+newPRUs.size());
        r.setValue(SHARE_NAME, ezShareName);
        newPRUs.add(r);
    }

    public int getNumberOfExistingPRUs() {
        return existingPRUs.getValues().size();
    }

    public List<Row> getNewPRUs() {
        return newPRUs;
    }

    public List<String> getNewPRUValues() {
        return newPRUs.stream().map(r -> r.getValueStr(SHARE_NAME)).collect(Collectors.toList());
    }

    public void saveDone() {
        existingPRUs.getValues().addAll(newPRUs);
        newPRUs.clear();
    }

    public PRU createDeepCopy() {
        PRU copy = new PRU(existingPRUs.createDeepCopy());
        copy.newPRUs.addAll(newPRUs.stream().map(Row::createDeepCopy).collect(Collectors.toList()));
        return copy;
    }
}
