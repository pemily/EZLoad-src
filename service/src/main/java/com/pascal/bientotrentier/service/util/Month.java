package com.pascal.bientotrentier.service.util;

import com.pascal.bientotrentier.service.model.BRDate;

public class Month {
    private int month;
    private int year;

    public Month(int year, int month){
        this.year = year;
        this.month = month;
    }

    public Month(BRDate date){
        this.year = date.getYear();
        this.month = date.getMonth();
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "Month{" +
                "year=" + year +
                ", month=" + month +
                '}';
    }
}
