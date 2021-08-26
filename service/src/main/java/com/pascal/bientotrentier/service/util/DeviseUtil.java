package com.pascal.bientotrentier.service.util;

import com.pascal.bientotrentier.service.model.BRDevise;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DeviseUtil {

    // https://en.wikipedia.org/wiki/Currency

    private static List<BRDevise> devises = Arrays.asList(
            new BRDevise("USD", "$"),
            new BRDevise("EUR", "€"),
            new BRDevise("JPY", "¥"),
            new BRDevise("GBP", "£"),
            new BRDevise("GBX", "£"), // non present sur le site, mais ezportfolio utilse gbx
            new BRDevise("AUD", "A$"),
            new BRDevise("CAD", "C$"),
            new BRDevise("CHF", "CHF"),
            new BRDevise("CNY", "元"),
            new BRDevise("HKD", "HK$"),
            new BRDevise("NZD", "NZ$"),
            new BRDevise("SEK", "kr"),
            new BRDevise("KRW", "₩"),
            new BRDevise("SGD", "S$"),
            new BRDevise("NOK", "Kr"),
            new BRDevise("DKK", "Kr")
            );


    public static BRDevise foundByCode(String code) throws BRException {
        Optional<BRDevise> optDevise = devises.stream().filter(d -> d.getCode().equals(code)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Devise with code: "+code+" not found"));
    }


}
