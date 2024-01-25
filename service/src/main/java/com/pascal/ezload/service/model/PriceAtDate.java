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

public class PriceAtDate {

    private Float price;
    private EZDate date;
    private boolean estimated;

    public PriceAtDate(){}

    public PriceAtDate(EZDate date, float price, boolean estimated){
        this.date = date;
        this.price = price;
        this.estimated = estimated;
    }

    public PriceAtDate(EZDate date, boolean estimated){
        this.date = date;
        this.price = null; // no value at this date
        this.estimated = estimated;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public EZDate getDate() {
        return date;
    }

    public void setDate(EZDate date) {
        this.date = date;
    }

    public boolean isEstimated(){
        return estimated;
    }

    public String toString(){
        return date.toString()+(estimated ? " estimated" : "")+" price: "+price;
    }
}
