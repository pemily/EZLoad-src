package com.pascal.ezload.service.model;

import java.util.Objects;

public class EZDate {
    private int day;
    private int month;
    private int year;

    // month => between 1 - 12
    // day => between 1 - 31
    public EZDate(int year, int month, int day){
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public String toEzPortoflioDate(){
        return leadingZero(day)+"/"+leadingZero(month)+"/"+year;
    }

    public String toDate(char separator){
        return year+""+separator+leadingZero(month)+separator+leadingZero(day);
    }

    public static EZDate parseFrenchDate(String date, char separator) {
        if (date == null) return null;
        String elem[] = date.trim().split(separator+"");
        // la valeur de la Date en String dans une Row est: dd/mm/yyyy (a cause du choix de ezPortfolio)
        if (elem.length != 3) return null;
        try {
            return new EZDate(Integer.parseInt(elem[2]), Integer.parseInt(elem[1]), Integer.parseInt(elem[0]));
        }
        catch(NumberFormatException ne){
            return null;
        }
    }

    public boolean isValid(){
        // here => check that the dd is between 1 and 31
        // here => check that the mm is between 1 and 12
        // here => check that the yyyy is between 2015 and 2030
        try {
            if (day < 1 || day > 31) return false;
            if (month < 1 || month > 12) return false;
            if (year < 2015 || year > 2030) return false;
            return true;
        }
        catch(Throwable e){
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EZDate EZDate = (EZDate) o;
        return day == EZDate.day && month == EZDate.month && year == EZDate.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, month, year);
    }

    public boolean isBeforeOrEquals(EZDate d) {
        return toDate('/').compareTo(d.toDate('/')) <= 0;
    }

    public static String leadingZero(int i){
        return i <= 9 ? "0"+i : ""+i;
    }

    @Override
    public String toString() {
        return "EZDate{" +
                "day=" + day +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}
