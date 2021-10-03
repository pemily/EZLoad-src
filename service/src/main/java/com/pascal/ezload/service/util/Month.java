package com.pascal.ezload.service.util;

import com.pascal.ezload.service.model.EZDate;

public class Month {
    private int month;
    private int year;

    public Month(int year, int month){
        this.year = year;
        this.month = month;
    }

    public Month(EZDate date){
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
