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
package com.pascal.ezload.service.exporter.ezEdition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pascal.ezload.service.model.EZDate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.pascal.ezload.service.util.NumberUtils.str2Float;
import static com.pascal.ezload.service.util.NumberUtils.str2Int;

public class EzData {

    private Map<String, String> data = new HashMap<>();

    public EzData(){
    }

    public EzData(EzData data) {
        this.data = new HashMap<>(data.data);
    }

    public EzData(Map<String, String> data){
        this.data = data;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @JsonIgnore
    public void put(EzDataKey key, String value){
        String val = value == null ? "" : value;
        if (containsKey(key.getName()) && !Objects.equals(val, get(key))) {
//            throw new RuntimeException("There is already a variable with this key: " + key + " new Value: " + val+ "  EZData is: "+data.toString());
        }
        this.data.put(key.getName(), val);
    }

    @JsonIgnore
    public String get(String name) {
        return this.data.get(name);
    }

    @JsonIgnore
    public String get(EzDataKey k) {
        return this.data.get(k.getName());
    }

    @JsonIgnore
    public int getInt(EzDataKey k){
        return str2Int(get(k));
    }

    @JsonIgnore
    public float getFloat(EzDataKey k){
        return str2Float(get(k));
    }

    @JsonIgnore
    public EZDate getDate(EzDataKey k) {
        String date = get(k);
        if (date == null) return null;
        return EZDate.parseFrenchDate(date, '/');
    }

    @JsonIgnore
    public boolean containsKey(String name) {
        return this.data.containsKey(name);
    }

    public String generateId(){
        return this.data.hashCode()+"";
    }

    public String toString(){
        return this.data.toString();
    }
}
