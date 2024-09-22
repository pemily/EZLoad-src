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
package com.pascal.ezload.common.util;

import java.io.File;

public class FileValue {

    private final Checkable<?> checkable;
    private final String field;
    private final String value;

    public FileValue(Checkable<?> checkable, String field, String value){
        this.checkable = checkable;
        this.field = field;
        this.value = value;
    }

    public FileValue checkRequired(){
        if (value == null || !new File(value).exists()){
            checkable.setErrorMsg(field, "Le repertoire n'existe pas");
        }
        return this;
    }

    public FileValue checkDirectory(){
        if (value != null && new File(value).exists()){
            if (!new File(value).isDirectory())
                checkable.setErrorMsg(field, "Cette valeur ne représente pas un repertoire");
        }
        return this;
    }

    public FileValue checkFile(){
        if (value != null && new File(value).exists()){
            if (!new File(value).isFile())
                checkable.setErrorMsg(field, "Cette valeur ne représente pas un fichier");
        }
        return this;
    }

}