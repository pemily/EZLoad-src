package com.pascal.ezload.service.util;

import com.pascal.ezload.service.model.EZCountry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CountryUtil {

    private static List<EZCountry> countries = Arrays.asList(
            new EZCountry("DE", "Allemagne"),
            new EZCountry("GB", "Angleterre"),
            new EZCountry("AU", "Australie"),
            new EZCountry("AT", "Autriche"),
            new EZCountry("BE", "Belgique"),
            new EZCountry("CA", "Canada"),
            new EZCountry("ES", "Espagne"),
            new EZCountry("US", "Etats-Unis"),
            new EZCountry("FI", "Finlande"),
            new EZCountry("FR", "France"),
            new EZCountry("HK", "Hong-Kong"),
            new EZCountry("IE", "Irlande"),
            new EZCountry("IT", "Italie"),
            new EZCountry("LU", "Luxembourg"),
            new EZCountry("NO", "Norvège"),
            new EZCountry("NL", "Pays-Bas"),
            new EZCountry("PT", "Portugal"),
            new EZCountry("CH", "Suisse"),
            new EZCountry("CN", "Chine"),
            new EZCountry("JP", "Japon")
    );

    public static EZCountry foundByCode(String code) throws BRException {
        Optional<EZCountry> optDevise = countries.stream().filter(d -> d.getCode().equals(code)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Country with code: "+code+" not found"));
    }

    public static EZCountry foundByName(String name) throws BRException {
        Optional<EZCountry> optDevise = countries.stream().filter(d -> d.getName().equals(name)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Country with code: "+name+" not found"));
    }
}

