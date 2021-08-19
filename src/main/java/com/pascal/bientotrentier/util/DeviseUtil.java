package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.model.BRDevise;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DeviseUtil {

    private static List<BRDevise> devises = Arrays.asList(
            new BRDevise("USD", "$"),
            new BRDevise("EUR", "€"),
            new BRDevise("DKK", "Kr"),
            new BRDevise("CAD", "$ CA"),
            new BRDevise("CHF", "Fr"),
            new BRDevise("GBP", "£"),
            new BRDevise("JPY", "¥"),
            new BRDevise("JPY", "¥"),
            new BRDevise("NOK", "Kr"),
            new BRDevise("KRW", "₩")

            );


    public static BRDevise foundByCode(String code) throws BRException {
        Optional<BRDevise> optDevise = devises.stream().filter(d -> d.getCode().equals(code)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Devise with code: "+code+" not found"));
    }


}
