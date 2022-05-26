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

