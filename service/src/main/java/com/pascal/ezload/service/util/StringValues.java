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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;


public class StringValues {
    private final Checkable<?> checkable;
    private final String field;
    private final String[] values;

    public StringValues(Checkable<?> checkable, String field, String[] values){
        this.checkable = checkable;
        this.field = field;
        this.values = values;
    }

    public StringValues checkRequired(){
        if (Arrays.stream(values).allMatch(StringUtils::isBlank)){
            checkable.setErrorMsg(field, "Cette valeur ne doit pas être vide");
        }
        return this;
    }

}