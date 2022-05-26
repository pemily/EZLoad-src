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
package com.pascal.ezload.service.exporter.rules.dividends.calendarDividends;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.rules.dividends.DividendsAlgo;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.FinanceTools;
import com.pascal.ezload.service.util.ModelUtils;
import com.pascal.ezload.service.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DividendsCalendar extends DividendsAlgo {

    // return true if update, false else
    public boolean compute(Reporting reporting, EzPortefeuilleEdition ezPortefeuilleEdition, MainSettings.DividendCalendarConfig algoConfig, List<FinanceTools.Dividend> allDividends){
        Function<FinanceTools.Dividend, EZDate> dateSelector = getDividendYear(algoConfig.getDateSelector());

        List<FinanceTools.Dividend> oneYearDividends = null;
        if (algoConfig.getYearSelector() == MainSettings.EnumAlgoYearSelector.ANNEE_EN_COURS)
            oneYearDividends = getCurrentYearDividends(allDividends, dateSelector);
        else
            oneYearDividends = getLastYearDividends(allDividends, getDividendYear(algoConfig.getDateSelector()));


        if (algoConfig.getPercentSelector() == MainSettings.EnumPercentSelector.ADAPTATIF)
            return computeCalendarWithAdaptativePercent(reporting, oneYearDividends, ezPortefeuilleEdition, dateSelector);
        else
            return computeCalendarWithRegularPercent(reporting, oneYearDividends, ezPortefeuilleEdition, dateSelector);
    }

    // le meme pourcentage dans toutes les colonnes du calendrier
    private boolean computeCalendarWithRegularPercent(Reporting reporting, List<FinanceTools.Dividend> dividends, EzPortefeuilleEdition ezPortefeuilleEdition, Function<FinanceTools.Dividend, EZDate> dateSelector){
        Set<Integer> monthWithDividends =  dividends.stream()
                                                    .collect(Collectors.groupingBy(d -> dateSelector.apply(d).getMonth()))
                                                    .keySet();

        float percentForMonth = 100f / monthWithDividends.size();
        String value = ModelUtils.float2Str(percentForMonth);
        String percentValueForYear = ModelUtils.float2Str(percentForMonth / 100f);

        boolean result = false;
        for (int month = 1; month <= 12; month++) {
            String testValue = monthWithDividends.contains(month) ? percentValueForYear : "";

            if (evalNumberOrPercent(testValue) != evalNumberOrPercent(ezPortefeuilleEdition.getMonthlyDividend(month))) {
                result = true;
                ezPortefeuilleEdition.setMonthlyDividend(month, testValue.equals("") ? "" : value+"%");
            }
        }
        return result;
    }

    // le pourcentage s'adapte au montant annuel des dividendes
    private boolean computeCalendarWithAdaptativePercent(Reporting reporting, List<FinanceTools.Dividend> dividends, EzPortefeuilleEdition ezPortefeuilleEdition, Function<FinanceTools.Dividend, EZDate> dateSelector){
        Map<Integer, List<FinanceTools.Dividend>> monthWithDividends =  dividends.stream()
                                                    .collect(Collectors.groupingBy(d -> dateSelector.apply(d).getMonth()));

        float totalAmount = ModelUtils.str2Float(sumOfAllDividends(reporting, dividends));

        boolean result = false;
        for (int month = 1; month <= 12; month++) {
            String newValue;
            if (totalAmount != 0 && monthWithDividends.containsKey(month)) {
                float monthAmount = ModelUtils.str2Float(sumOfAllDividends(reporting, monthWithDividends.get(month)));
                newValue = ModelUtils.float2Str(monthAmount * 100f / totalAmount)+"%";
            }
            else newValue = "";

            if (evalNumberOrPercent(newValue) != evalNumberOrPercent(ezPortefeuilleEdition.getMonthlyDividend(month))) {
                result = true;
                ezPortefeuilleEdition.setMonthlyDividend(month, newValue);
            }
        }
        return result;
    }
}
