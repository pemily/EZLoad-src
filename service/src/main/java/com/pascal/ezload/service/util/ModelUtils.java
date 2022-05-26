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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;

import java.util.Arrays;
import java.util.Optional;

public class ModelUtils {
    private static final int NB_AFTER_COMMA = 6; // 6 chiffres apres la virgule max

    public static String normalizeAmount(String amount) {
        String result = amount;
        // remove the + in front of positive number
        // the separator must be , and not .
        // the space to separate the thousands must be remove
        // example:
        //    +1 248.43 => 1248,43
        // maximum 6 number after the comma

        if (result.startsWith("+")){
            result = result.substring(1);
        }
        if (result.startsWith("-")){
            if (result.charAt(1) < '0' || result.charAt(1) > '9') return amount; // ce n'est pas un nombre
        }
        else if (result.charAt(0) < '0' || result.charAt(0) > '9') return amount; // ce n'est pas un nombre

        result = result.replace('.', ',').replace(" ", "");
        int commaIndex = result.indexOf(",");
        if (commaIndex != -1){
            if (result.length() > commaIndex+NB_AFTER_COMMA){
                result = result.substring(0, commaIndex+NB_AFTER_COMMA+1);
            }

            while (result.endsWith("0")){
                result = result.substring(0, result.length()-1);
            }
        }
        if (result.endsWith(",")){
            result = result.substring(0, result.length()-1);
        }
        return result;
    }

    public static int normalizeNumber(String quantite) {
        return Integer.parseInt(quantite);
    }

    public static String toJson(EZModel model){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(model);
    }

    public static float str2Float(String v) {
        if (v == null) return 0;
        return Float.parseFloat(v.replace(',','.')
                .replace(' ', ' ') // google drive add some NNBSP as a thousand separator
                .replace(" ",""));
    }

    public static int str2Int(String v) {
        if (v == null) return 0;
        return Integer.parseInt(v.replace(' ', ' ') // google drive add some NNBSP as a thousand separator
                .replace(" ",""));
    }


    public static String float2Str(float v) {
        return normalizeAmount(String.format("%s", v)); // 5 digit apres la virgule
    }

    public static String double2Str(double v) {
        return normalizeAmount(String.format("%s", v)); // 5 digit apres la virgule
    }

 
}
