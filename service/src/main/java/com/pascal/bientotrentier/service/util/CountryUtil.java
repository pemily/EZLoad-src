package com.pascal.bientotrentier.service.util;

import com.pascal.bientotrentier.service.model.BRCountry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CountryUtil {

    private static List<BRCountry> countries = Arrays.asList(
            new BRCountry("DE", "Allemagne"),
            new BRCountry("GB", "Angleterre"),
            new BRCountry("AU", "Australie"),
            new BRCountry("AT", "Autriche"),
            new BRCountry("BE", "Belgique"),
            new BRCountry("CA", "Canada"),
            new BRCountry("ES", "Espagne"),
            new BRCountry("US", "Etats-Unis"),
            new BRCountry("FI", "Finlande"),
            new BRCountry("FR", "France"),
            new BRCountry("HK", "Hong-Kong"),
            new BRCountry("IE", "Irlande"),
            new BRCountry("IT", "Italie"),
            new BRCountry("LU", "Luxembourg"),
            new BRCountry("NO", "Norvège"),
            new BRCountry("NL", "Pays-Bas"),
            new BRCountry("PT", "Portugal"),
            new BRCountry("CH", "Suisse"),
            new BRCountry("CN", "Chine"),
            new BRCountry("JP", "Japon")
    );

    public static BRCountry foundByCode(String code) throws BRException {
        Optional<BRCountry> optDevise = countries.stream().filter(d -> d.getCode().equals(code)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Country with code: "+code+" not found"));
    }


}

