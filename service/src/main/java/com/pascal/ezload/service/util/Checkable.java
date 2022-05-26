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
package com.pascal.ezload.service.util;

import java.util.HashMap;
import java.util.Map;

public abstract class Checkable<T> {

    private Map<String, String> field2ErrorMsg = new HashMap<>();

    public Map<String, String> getField2ErrorMsg() {
        return field2ErrorMsg != null ? field2ErrorMsg : (field2ErrorMsg = new HashMap<>());
    }

    public void setField2ErrorMsg(Map<String, String> field2ErrorMsg) {
        this.field2ErrorMsg = field2ErrorMsg;
    }

    public abstract T validate();

    public String getErrorMsg(String fieldName){
        return this.getField2ErrorMsg().get(fieldName);
    }

    public void setErrorMsg(String fieldName, String errorMsg){
        if (errorMsg != null) {
            this.getField2ErrorMsg().put(fieldName, errorMsg);
        }
    }

    public void clearErrors(){
        setField2ErrorMsg(null);
    }

    public boolean hasError(){
        return field2ErrorMsg != null && !field2ErrorMsg.isEmpty();
    }
}
