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
        int refIndex = 5; // the first row start at 5 in ezPortfolio
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
        newPRUs.add(new Row(ezShareName));
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
