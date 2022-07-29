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
package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.model.BrokerCustomCode;
import com.pascal.ezload.service.util.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.stream.Collectors;

public class BourseDirectCustomCode implements BrokerCustomCode {
    private LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    @Override
    public Optional<Map<String, Object>> searchActionInDifferentMarket(String actionCode, List<Map<String, Object>> data, EzData ezData) {
        List<Map<String, Object>> dataReduced = reduce(actionCode, ezData, data,
                this::sortByISIN,
                this::sortByStock,
                this::sortByLieu,
                this::sortByCountryCode,
                this::sortByBestName
                 );
        return dataReduced.size() > 0 ? Optional.of(dataReduced.get(0)) : Optional.empty();
    }

    public interface IReducer {
        int  reduce(String actionCode, EzData ezData, Map<String, Object> d1, Map<String, Object> d2);
    }

    private List<Map<String, Object>> reduce(String actionCode, EzData ezData, List<Map<String, Object>> data, IReducer... reducers){
        if (data.size() == 1) return data;

        return data.stream().sorted(
                (d1 , d2) -> {
                    for (IReducer reducer : reducers){
                        int r = reducer.reduce(actionCode, ezData, d1, d2);
                        if (r != 0) return r;
                    }
                    return 0;
                }
        )
        .collect(Collectors.toList());
    }


    private int sortByStock(String actionCode, EzData ezData, Map<String, Object> d1, Map<String, Object> d2) {
        boolean isStock1 = isStock(d1);
        boolean isStock2 = isStock(d2);
        if (isStock1 && isStock2) return 0;
        if (isStock1) return -1;
        if (isStock2) return 1;
        return 0;
    }

    private boolean isStock(Map<String, Object> data){
        Map<String, Object> sinara = (Map<String, Object>) data.get("sinara");
        String nature = null;
        if (sinara != null){
            nature = sinara.get("nature") instanceof String ? (String) sinara.get("nature") : null;
        }
        // si nature n'est pas une string, on a pas reussis a determiné, mais ca peut etre un stock quand meme, donc dans le doute
        // on l'elimine pas
        return nature == null || nature.equals("stock");
    }

    private int sortByLieu(String actionCode, EzData ezData, Map<String, Object> d1, Map<String, Object> d2) {
        String lieu = ezData.get("ezOperation_Lieu");
        /* I saw the values from pdf:
                BORSE BERLIN EQUIDUCT TRADING - BERL
                NEW YORK STOCK EXCHANGE, INC.
                NASDAQ/NGS (GLOBAL SELECT MARKET)
         */
        if (!StringUtils.isBlank(lieu)){
            if (d1.get("label") != null && d2.get("label") != null) {
                int lev1 = levenshteinDistance.apply(lieu, (String) d1.get("label"));
                int lev2 = levenshteinDistance.apply(lieu, (String) d2.get("label"));
                if (lev1 > 2 && lev2 > 2) return 0; // both are very different use the next criteria
                if (lev1 <= 2 && lev2 <= 2) return 0; // both are too close to use this criteria only
                if (lev1 <= 2) return -1; // d1 is close, d2 is far
                return 1; // d2 is close, d1 is far
            }
            else return 0;
        }
        else return 0; // do not perform a change in the list order
    }


    private int sortByISIN(String actionCode, EzData ezData, Map<String, Object> d1, Map<String, Object> d2) {
        String isin1 = (String) d1.get("isin");
        String isin2 = (String) d2.get("isin");
        if (isin1 != null && isin2 != null){
            if (isin1.equals(isin2)) return 0;
            if (isin1.equals(actionCode)) return -1;
            return 1;

        }
        else if (isin1 == null){
            if (isin2 == null) return 0;
            return isin2.equals(actionCode) ? 1 : -1;
        }
        return isin1.equals(actionCode) ? -1 : 1;
    }

    private int sortByCountryCode(String actionCode, EzData ezData, Map<String, Object> d1, Map<String, Object> d2) {
        if (actionCode.length() <= 3) throw new IllegalArgumentException("Action Code is too short: "+ actionCode + " ezData contains: "+ ezData);
        String countryCode = actionCode.substring(0, 2);

        String countryCode1 = (String) d1.get("Country");
        String countryCode2 = (String) d2.get("Country");
        if (countryCode1 != null && countryCode2 != null){
            if (countryCode1.equals(countryCode2)) return 0;
            if (countryCode1.equals(countryCode)) return -1;
            return 1;

        }
        else if (countryCode1 == null){
            if (countryCode2 == null) return 0;
            return countryCode2.equals(countryCode) ? 1 : -1;
        }
        return countryCode1.equals(countryCode) ? -1 : 1;
    }

    private int sortByBestName(String actionCode, EzData ezData, Map<String, Object> d1, Map<String, Object> d2) {
        String actionName = ezData.get("ezOperation_INFO3");
        if (!StringUtils.isBlank(actionName)) {
            return distanceActionName(d1, actionName) - distanceActionName(d2, actionName);
        }
        return 0;
    }

    private int distanceActionName(Map<String, Object> d1, String actionName) {
        String name1 = (String) d1.get("name");
        String name2 = (String) d1.get("Name");
        if (name1 != null && name2 != null) {
            return Math.min(levenshteinDistance.apply(actionName, name1),
                    levenshteinDistance.apply(actionName, name2));
        }
        if (name1 != null) {
            return levenshteinDistance.apply(actionName, name1);
        }
        if (name2 != null) {
            return levenshteinDistance.apply(actionName, name2);
        }
        return Integer.MAX_VALUE;
    }


}
