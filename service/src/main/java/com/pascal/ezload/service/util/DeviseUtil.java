package com.pascal.ezload.service.util;

import com.pascal.ezload.service.model.EZDevise;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DeviseUtil {

    // https://en.wikipedia.org/wiki/Currency

    private static List<EZDevise> devises = Arrays.asList(
            new EZDevise("USD", "$"),
            new EZDevise("EUR", "€"),
            new EZDevise("JPY", "¥"),
            new EZDevise("GBP", "£"),
            new EZDevise("GBX", "£"), // non present sur le site, mais ezportfolio utilse gbx
            new EZDevise("AUD", "A$"),
            new EZDevise("CAD", "C$"),
            new EZDevise("CHF", "CHF"),
            new EZDevise("CNY", "元"),
            new EZDevise("HKD", "HK$"),
            new EZDevise("NZD", "NZ$"),
            new EZDevise("SEK", "kr"),
            new EZDevise("KRW", "₩"),
            new EZDevise("SGD", "S$"),
            new EZDevise("NOK", "Kr"),
            new EZDevise("DKK", "Kr")
            );


    public static EZDevise foundByCode(String code) throws BRException {
        Optional<EZDevise> optDevise = devises.stream().filter(d -> d.getCode().equals(code)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Devise with code: "+code+" not found"));
    }


}
