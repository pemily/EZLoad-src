/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.common.util;

import com.pascal.ezload.common.model.EZCountry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CountryUtil {

    // https://documentation.abes.fr/sudoc/formats/CodesPays.htm
    private static List<EZCountry> countries = Arrays.asList(
            new EZCountry("AT", "Autriche"),
            new EZCountry("AU", "Australie"),
            new EZCountry("BE", "Belgique"),
            new EZCountry("CA", "Canada"),
            new EZCountry("CH", "Suisse"),
            new EZCountry("CN", "Chine"),
            new EZCountry("DE", "Allemagne"),
            new EZCountry("DK", "Danemark"),
            new EZCountry("EE", "Estonie"),
            new EZCountry("ES", "Espagne"),
            new EZCountry("FI", "Finlande"),
            new EZCountry("FR", "France"),
            new EZCountry("GB", "Angleterre"),
            new EZCountry("HK", "Hong-Kong"),
            new EZCountry("IE", "Irlande"),
            new EZCountry("IN", "Inde"),
            new EZCountry("IS", "Islande"),
            new EZCountry("IT", "Italie"),
            new EZCountry("JP", "Japon"),
            new EZCountry("KR", "Corée"),
            new EZCountry("LT", "Lituanie"),
            new EZCountry("LU", "Luxembourg"),
            new EZCountry("LV", "Lettonie"),
            new EZCountry("NL", "Pays-Bas"),
            new EZCountry("NO", "Norvège"),
            new EZCountry("PT", "Portugal"),
            new EZCountry("SE", "Suède"),
            new EZCountry("TW", "Taiwan"),
            new EZCountry("US", "Etats-Unis"),
            new EZCountry("ZA", "Afrique du Sud")
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
