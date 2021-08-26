package com.pascal.bientotrentier.service.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pascal.bientotrentier.service.model.BRDate;
import com.pascal.bientotrentier.service.model.BRModel;
import org.apache.commons.lang3.StringUtils;

public class ModelUtils {

    public static String normalizeAmount(String amount) {
        // remove the + in front of positive number
        // the separator must be , and not .
        // the space to separate the thousands must be remove
        // example:
        //    +1 248.43 => 1248,43
        if (amount.startsWith("+")){
            amount = amount.substring(1);
        }
        amount = amount.replace('.', ',').replace(" ", "");
        if (amount.contains(",")){
            while (amount.endsWith("0")){
                amount = amount.substring(0, amount.length()-1);
            }
        }
        if (amount.endsWith(",")){
            amount = amount.substring(0, amount.length()-1);
        }
        return amount;
    }

    public static int normalizeNumber(String quantite) {
        return Integer.parseInt(quantite);
    }

    public static String toJson(BRModel model){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(model);
    }

    public static float str2Float(String v) {
        if (v == null) return 0;
        return Float.parseFloat(v.replace(',','.')
                .replace(' ', ' ') // google drive add some NNBSP as a thousand separator
                .replace(" ",""));
    }


    public static String float2Str(float v) {
        return normalizeAmount(v+"");
    }
}
