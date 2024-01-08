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

    public static EZDate yearPeriod(int year){
        return new EZDate(year, -1, -1);
    }

    public static EZDate monthPeriod(int year, int month){
        return new EZDate(year, month, -1);
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

    public EZDate plusMonthes(int d) {
        return toEZDate(toLocalDate().plusMonths(d));
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

    public long nbOfMonthesTo(EZDate to) {
        return toLocalDate().until(LocalDate.of(to.year, to.month, to.day), ChronoUnit.MONTHS);
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
        return isBefore(d.plusDays(1));
    }
    public boolean isAfterOrEquals(EZDate d) {
        return isAfter(d.minusDays(1));
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
        if (isPeriod()) throw new IllegalStateException("cette date est une période"+day+"/"+month+"/"+year);
        return leadingZero(day)+"/"+leadingZero(month)+"/"+year;
    }

    public String toYYYYMMDD(){
        if (isPeriod()) {
            if (month == -1) return year+"/12/31"; // yearly period
            if (month == 12) return year+"/12/31";
            LocalDate l = LocalDate.of(year, month+1, 1).minusDays(1);
            return year+"/"+leadingZero(month)+"/"+l.getDayOfMonth(); // je vais au 1er du mois suivant puis je recule d'un jour (pour eviter de me tromper sur le 30/31/28)
        }
        return year+"/"+leadingZero(month)+"/"+leadingZero(day);
    }

    public String toYYYYMM(){
        return year+"/"+leadingZero(month);
    }

    public LocalDate toLocalDate(){
        if (month == -1){
            return LocalDate.of(year, 12, 31);
        }
        if (day == -1){
            if (month == 12){
                return LocalDate.of(year, month, 31);
            }
            return LocalDate.of(year, month+1, 1).minusDays(1); // je vais au 1er du mois suivant puis je recule d'un jour (pour eviter de me tromper sur le 30/31/28)
        }
        return LocalDate.of(year, month, day);
    }
    
    public String toDate(char separator){
        if (isPeriod()){
            if (month == -1) return year+"";
            return year + separator + leadingZero(month);
        }
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
        if (isPeriod()) // c'est une periode
            return month == -1 ? year+"" : leadingZero(month)+"/"+year;
        return toEzPortoflioDate();
    }

    @Override
    public int compareTo(EZDate date) {
        return (int) (this.toEpochSecond() - date.toEpochSecond());
    }


    private EZDate toEZDate(LocalDate localDate) {
        return new EZDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
    }


    public boolean contains(EZDate date) {
        if (!isPeriod()) return this.equals(date);
        return this.month == -1 ? date.getYear() == this.year : date.getYear() == this.year && date.getMonth() == this.month;
    }

    public EZDate createNextPeriod() {
        if (!isPeriod()) throw new IllegalStateException("This date does not represent a Period");
        if (this.month == -1) return new EZDate(this.year+1, -1, -1);
        if (this.month == 12) return new EZDate(this.year+1, 1, -1);
        return new EZDate(this.year, this.month+1, -1);
    }

    public boolean isPeriod(){
        return day == -1;
    }

    public EZDate endPeriodDate() {
        if (!isPeriod()) throw new IllegalStateException("This date does not represent a Period");
        if (month == -1) return new EZDate(this.year, 12, 31);
        if (month == 12) return new EZDate(this.year, 12, 31);
        return new EZDate(this.year, month+1, 1).yesterday();
    }

    public EZDate startPeriodDate() {
        if (!isPeriod()) throw new IllegalStateException("This date does not represent a Period");
        if (month == -1) return new EZDate(this.year, 1, 1);
        return new EZDate(this.year, 1, 1);
    }
}
