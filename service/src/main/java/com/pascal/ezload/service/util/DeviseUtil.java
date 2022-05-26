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
