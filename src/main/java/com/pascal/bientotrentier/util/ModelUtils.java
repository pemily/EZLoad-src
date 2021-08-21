package com.pascal.bientotrentier.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pascal.bientotrentier.model.BRModel;
import org.apache.commons.lang3.StringUtils;

public class ModelUtils {

    public static boolean isValidDate(String date){
        if (StringUtils.isBlank(date)) return false;
        // the returned date must be like with the french format: dd/mm/yyyy
        // here => check that the dd is between 1 and 31
        // here => check that the mm is between 1 and 12
        // here => check that the yyyy is between 2015 and 2030
        try {
            String[] s = date.split("/");
            if (s.length != 3) return false;
            int dd = Integer.parseInt(s[0]);
            int mm = Integer.parseInt(s[1]);
            int yyyy = Integer.parseInt(s[2]);
            if (dd < 1 || dd > 31) return false;
            if (mm < 1 || mm > 12) return false;
            if (yyyy < 2015 || yyyy > 2030) return false;
            return true;
        }
        catch(Throwable e){
            return false;
        }
    }

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
        return Float.parseFloat(v.replace(',','.'));
    }
}
