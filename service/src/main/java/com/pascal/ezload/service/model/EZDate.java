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
package com.pascal.ezload.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class EZDate implements Comparable<EZDate> {
    private final int day;
    private final int month;
    private final int year;

    public EZDate(long epochSecond){ // seconds from 1970/01/01
        LocalDate localDate = Instant.ofEpochSecond(epochSecond).atZone(ZoneId.systemDefault()).toLocalDate();
        this.year = localDate.getYear();
        this.month = localDate.getMonthValue();
        this.day = localDate.getDayOfMonth();
    }

    public EZDate(String dateValue){
        // for json deserialization
        EZDate d = parseFrenchDate(dateValue, '/');
        day = d.getDay();
        month = d.getMonth();
        year = d.getYear();
    }

    // month => between 1 - 12
    // day => between 1 - 31
    public EZDate(int year, int month, int day){
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static EZDate today(){
        return new EZDate(Instant.now().getEpochSecond());
    }

    public EZDate yesterday() {
        return toEZDate(toLocalDate().minusDays(1));
    }

    public EZDate tomorrow(){
        return toEZDate(toLocalDate().plusDays(1));
    }

    public EZDate plusDays(int d) {
        return toEZDate(toLocalDate().plusDays(d));
    }

    public EZDate minusDays(int d) {
        return toEZDate(toLocalDate().minusDays(d));
    }

    public EZDate minusYears(int years) {
        return new EZDate(year-years, month, day);
    }

    public EZDate plusYears(int years) {
        return new EZDate(year+years, month, day);
    }

    public long nbOfDaysTo(EZDate to) {
        return toLocalDate().until(LocalDate.of(to.year, to.month, to.day), ChronoUnit.DAYS);
    }

    // return true if this is after dateToTest
    public boolean isAfter(EZDate dateToTest){
        if (this.equals(dateToTest)) return false;
        return this.toEpochSecond() > dateToTest.toEpochSecond();
    }

    // return true if this is before dateToTest
    public boolean isBefore(EZDate dateToTest){
        if (this.equals(dateToTest)) return false;
        return this.toEpochSecond() < dateToTest.toEpochSecond();
    }

    public boolean isBeforeOrEquals(EZDate d) {
        return toDate('/').compareTo(d.toDate('/')) <= 0;
    }
    public boolean isAfterOrEquals(EZDate d) {
        return toDate('/').compareTo(d.toDate('/')) >= 0;
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

    public String toYYYYMMDD(){
        return year+"/"+leadingZero(month)+"/"+leadingZero(day);
    }
/*
    public String toDDMMYYYY(){
        return leadingZero(day)+"/"+leadingZero(month)+"/"+year;
    }

    public String toMMYY(String separator){
        return month+separator+(year+"").substring(2);
    }

    public String toMMstrYY(String separator){
        return toMonthStr(month)+separator+(year+"").substring(2);
    }

    private String toMonthStr(int month) {
        switch (month){
            case 1: return "Jan";
            case 2: return "Fev";
            case 3: return "Mar";
            case 4: return "Avr";
            case 5: return "Mai";
            case 6: return "Juin";
            case 7: return "Juil";
            case 8: return "Aout";
            case 9: return "Sept";
            case 10: return "Oct";
            case 11: return "Nov";
            case 12: return "Dec";
        }
        throw new IllegalArgumentException("Erreur sur le mois :"+month);
    }
*/
    public LocalDate toLocalDate(){
        return LocalDate.of(year, month, day);
    }
    
    public String toDate(char separator){
        return year+separator+leadingZero(month)+separator+leadingZero(day);
    }

    // millisec since 1970/01/01
    public long toEpochSecond(){
        return toLocalDate().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
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

    public static EZDate parseYYYMMDDDate(String date, char separator) {
        if (date == null) return null;
        String elem[] = date.trim().split(separator+"");
        // la valeur de la Date en String dans une Row est: yyyy/mm/dd
        if (elem.length != 3) return null;
        try {
            return new EZDate(Integer.parseInt(elem[0]), Integer.parseInt(elem[1]), Integer.parseInt(elem[2]));
        }
        catch(NumberFormatException ne){
            return null;
        }
    }

    @JsonIgnore
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

    @Override
    public int compareTo(EZDate date) {
        return (int) (this.toEpochSecond() - date.toEpochSecond());
    }


    private EZDate toEZDate(LocalDate localDate) {
        return new EZDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
    }


}
