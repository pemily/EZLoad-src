package com.pascal.ezload.service.util;

public class NumberUtils {


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
