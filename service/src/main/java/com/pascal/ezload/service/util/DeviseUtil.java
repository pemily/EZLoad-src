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
    public static final EZDevise SPECIAL_PERCENT = new EZDevise("PERCENT", "%");
    public static final EZDevise AUD = new EZDevise("AUD", "A$");
    public static final EZDevise CAD = new EZDevise("CAD", "C$");
    public static final EZDevise CHF = new EZDevise("CHF", "CHF");
    public static final EZDevise CNY = new EZDevise("CNY", "元");
    public static final EZDevise DKK = new EZDevise("DKK", "Kr");
    public static final EZDevise EUR = new EZDevise("EUR", "€");
    public static final EZDevise GBP = new EZDevise("GBP", "£");
    public static final EZDevise GBX = new EZDevise("GBX", "£"); // non present sur le site, mais ezportfolio utilse gbx
    public static final EZDevise HKD = new EZDevise("HKD", "HK$");
    public static final EZDevise INR = new EZDevise("INR", "₹");
    public static final EZDevise ISK = new EZDevise("ISK", "krona"); // cette monaie n'a pas de symbol court
    public static final EZDevise JPY = new EZDevise("JPY", "¥");
    public static final EZDevise KRW = new EZDevise("KRW", "₩");
    public static final EZDevise NOK = new EZDevise("NOK", "Kr");
    public static final EZDevise NZD = new EZDevise("NZD", "NZ$");
    public static final EZDevise SEK = new EZDevise("SEK", "kr");
    public static final EZDevise SGD = new EZDevise("SGD", "S$");
    public static final EZDevise TWD = new EZDevise("TWD", "NT$");
    public static final EZDevise USD = new EZDevise("USD", "$");
    public static final EZDevise ZAR = new EZDevise("ZAR", "R");

    // https://en.wikipedia.org/wiki/Currency

    private static List<EZDevise> devises = Arrays.asList(
            AUD,
            CAD,
            CHF,
            CNY,
            DKK,
            EUR,
            GBP,
            GBX,
            HKD,
            INR,
            ISK,
            JPY,
            KRW,
            NOK,
            NZD,
            SEK,
            SGD,
            TWD,
            USD,
            ZAR
            );


    public static EZDevise foundByCode(String code) throws BRException {
        Optional<EZDevise> optDevise = devises.stream().filter(d -> d.getCode().equalsIgnoreCase(code)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Devise with code: "+code+" not found"));
    }

    public static EZDevise foundBySymbol(String symbol) {
        Optional<EZDevise> optDevise = devises.stream().filter(d -> d.getSymbol().equalsIgnoreCase(symbol)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Devise with code: "+symbol+" not found"));
    }

    public static EZDevise foundBySymbolOrCode(String symbolOrCode) {
        try {
            return foundBySymbol(symbolOrCode);
        }
        catch (BRException e){
            return foundByCode(symbolOrCode);
        }
    }
}
