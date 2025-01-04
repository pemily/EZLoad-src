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
package com.pascal.ezload.common.model;

import java.util.HashMap;
import java.util.Map;

public class Price {

    public static final Price ZERO = new Price(0);
    public static final Price CENT = new Price(100);

    private Map<String, Tag> tags = null;
    private final Float value;
    private final boolean estimated;

    public Price(){
        this.value = null; // unknown value
        this.estimated = false;
    }

    public Price(Price price){
        if (price.value == null){
            this.value = null; // unknown value
            this.estimated = false;
        }
        else {
            this.value = price.value;
            this.estimated = price.estimated;
        }
        this.tags = price.tags == null ? null : new HashMap<>(tags);
    }

    public Price(float value){
        this(value, false);
    }

    public Price(float value, boolean estimated){
        this.value = value;
        this.estimated = estimated;
    }

    public Float getValue() {
        return value == null ? null : value;
    }

    public void addTag(String key, Tag value){
        if (tags == null) tags = new HashMap<>();
        tags.put(key, value);
    }

    public Tag getTag(String key){
        if (tags == null) return null;
        return tags.get(key);
    }

    public boolean isEstimated(){
        return estimated;
    }

    public String toString(){
        return (estimated ? " estimated" : "")+" value: "+ getValue() + " Tags: "+tags;
    }

    public Price minus(Price p) {
        Float newValue = null;
        if (this.value == null) {
            if (p.value != null){
                newValue = - p.value;
            }
        }
        else {
            if (p.value == null){
                newValue = this.value;
            }
            else {
                newValue = this.value - p.value;
            }
        }
        return newValue == null ? new Price() : new Price(newValue, estimated | p.isEstimated());
    }

    public Price plus(Price p) {
        Float newValue = null;
        if (this.value == null) {
            if (p.value != null){
                newValue = p.value;
            }
        }
        else {
            if (p.value == null){
                newValue = this.value;
            }
            else {
                newValue = this.value + p.value;
            }
        }
        return newValue == null ? new Price() : new Price(newValue, estimated | p.isEstimated());
    }

    public Price multiply(Price p) {
        Float newValue = null;
        if (this.value == null) {
            if (p.value != null){
                newValue = 0f;
            }
        }
        else {
            if (p.value == null){
                newValue = 0f;
            }
            else {
                newValue = this.value * p.value;
            }
        }
        return newValue == null ? new Price() : new Price(newValue, estimated | p.isEstimated());
    }

    public Price divide(Price p) {
        Float newValue;
        if (this.value == null) {
            if (p.value != null && p.value != 0){
                newValue = null;
            }
            else {
                throw new IllegalStateException("Division par zero");
            }
        }
        else {
            if (p.value == null || p.value == 0){
                throw new IllegalStateException("Division par zero");
            }
            else {
                newValue = this.value / p.getValue();
            }
        }
        return newValue == null ? new Price() : new Price(newValue, estimated | p.isEstimated());
    }

    public Price round(){
        int PRECISION = 100;
        return value == null ? new Price() : new Price(Math.round(value*PRECISION)/(float)PRECISION, estimated);
    }

    public Price reverse(){
        return value == null ? new Price() : new Price(-value, estimated);
    }
}
