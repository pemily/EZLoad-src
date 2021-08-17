package com.pascal.bientotrentier.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pascal.bientotrentier.model.BRModel;

public class ModelUtils {

    public static String normalizeDate(String date){
        return date;
    }

    public static String normalizeAmount(String amount) {
        return amount;
    }

    public static int normalizeNumber(String quantite) {
        return Integer.parseInt(quantite);
    }

    public static String toJson(BRModel model){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(model);
    }
}
