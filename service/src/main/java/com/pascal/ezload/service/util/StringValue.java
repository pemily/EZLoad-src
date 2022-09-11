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
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Arrays;


public class StringValue {
    private final Checkable<?> checkable;
    private final String field;
    private final String value;

    public StringValue(Checkable<?> checkable, String field, String value){
        this.checkable = checkable;
        this.field = field;
        this.value = value;
    }

    public StringValue checkRequired(){
        if (StringUtils.isBlank(value)){
            checkable.setErrorMsg(field, "Cette valeur ne doit pas être vide");
        }
        return this;
    }

    public StringValue validateWithForbidenValues(String... forbidenValues){
        if (Arrays.stream(forbidenValues).anyMatch(forbidenValue ->  forbidenValue.equals(value))){
            checkable.setErrorMsg(field, "Cette valeur est interdite");
        }
        return this;
    }

    public StringValue validateWithLimitedValues(String... acceptedValues){
        if (Arrays.stream(acceptedValues).noneMatch(acceptedValue ->  acceptedValue.equals(value))){
            checkable.setErrorMsg(field, "Valeurs possible: "+Arrays.asList(acceptedValues));
        }
        return this;
    }


    public void checkPrefixMatch(String matchPrefix){
        if (matchPrefix == null){
            if (!value.startsWith("https://"))
                checkable.setErrorMsg(field, "Cette valeur doit commencer par https://");
            else if (value.equals("https://"))
                checkable.setErrorMsg(field, "Cette valeur est incomplète");
            else
                checkable.setErrorMsg(field, null);
        }
        else {
            if (!value.startsWith(matchPrefix))
                checkable.setErrorMsg(field, "Cette valeur doit commencer par " + matchPrefix);
            else if (value.equals(matchPrefix))
                checkable.setErrorMsg(field, "Cette valeur est incomplète");
            else
                checkable.setErrorMsg(field, null);
        }
    }

    public void validateEmail() {
        if (!EmailValidator.getInstance().isValid(value)){
            checkable.setErrorMsg(field, "Email invalide");
        }
    }
}