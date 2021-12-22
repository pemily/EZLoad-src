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

    public static String normalizeAmount(String amount) {
        String result = amount;
        // remove the + in front of positive number
        // the separator must be , and not .
        // the space to separate the thousands must be remove
        // example:
        //    +1 248.43 => 1248,43
        if (result.startsWith("+")){
            result = result.substring(1);
        }
        if (result.startsWith("-")){
            if (result.charAt(1) < '0' || result.charAt(1) > '9') return amount; // ce n'est pas un nombre
        }
        else if (result.charAt(0) < '0' || result.charAt(0) > '9') return amount; // ce n'est pas un nombre

        result = result.replace('.', ',').replace(" ", "");
        if (result.contains(",")){
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
        return normalizeAmount(String.format("%.6f", v)); // 5 digit apres la virgule
    }

    public static String double2Str(double v) {
        return normalizeAmount(String.format("%.6f", v)); // 5 digit apres la virgule
    }


    public static EZDate getDateFromFile(String sourceFile){
        Optional<EnumEZBroker> optBroker = fromSourceFile(sourceFile);
        if (optBroker.isEmpty()) throw new IllegalStateException("Impossible to determine broker from source file: "+sourceFile);
        switch(optBroker.get()) {
            case BourseDirect: return BourseDirectDownloader.getDateFromPdfFilePath(sourceFile);
            default: throw new IllegalStateException("Unkown broker");
        }
    }

    public static Optional<EnumEZBroker> fromSourceFile(String reportSource){
        return Arrays.stream(EnumEZBroker.values()).filter(e -> reportSource.startsWith(e.getDirName())).findFirst();
    }
}
