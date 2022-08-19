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
package com.pascal.ezload.service.exporter.rules.dividends.annualDividends;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.rules.dividends.DividendsAlgo;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.finance.Dividend;
import com.pascal.ezload.service.util.finance.FinanceTools;

import java.util.List;
import java.util.function.Function;

public class AnnualDividendsAlgo extends DividendsAlgo {

    // return true if update, false else
    public boolean compute(Reporting reporting, EzPortefeuilleEdition ezPortefeuilleEdition, MainSettings.AnnualDividendConfig algoConfig, List<Dividend> allDividends){
        Function<Dividend, EZDate> dateSelector = getDividendYear(algoConfig.getDateSelector());

        List<Dividend> oneYearDividends;
        if (algoConfig.getYearSelector() == MainSettings.EnumAlgoYearSelector.ANNEE_EN_COURS)
            oneYearDividends = getCurrentYearDividends(allDividends, dateSelector);
        else
            oneYearDividends = getLastYearDividends(allDividends, getDividendYear(algoConfig.getDateSelector()));


        return computeAnnualDividends(reporting, oneYearDividends, ezPortefeuilleEdition);
    }


    // return true if update, false else
    private boolean computeAnnualDividends(Reporting reporting, List<Dividend> dividends, EzPortefeuilleEdition ezPortefeuilleEdition) {
        String sum = sumOfAllDividends(reporting, dividends);
        if (evalNumberOrPercent(sum) != evalNumberOrPercent(ezPortefeuilleEdition.getAnnualDividend())) {
            ezPortefeuilleEdition.setAnnualDividend(sum);
            return true;
        }
        return false;
    }

}
