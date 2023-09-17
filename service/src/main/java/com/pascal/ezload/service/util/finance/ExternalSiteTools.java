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
package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.sources.Reporting;

public class ExternalSiteTools {

    static Prices checkResult(Reporting reporting, EZShare ezShare, Prices sharePrices, long nbOfDays) {
        int nbOfPrices = sharePrices.getPrices().size();
        float percentOfData = nbOfPrices * 100f / nbOfDays;
        if (percentOfData > 50){
            return sharePrices;
        }
        reporting.info("Pas assez de données récupérés pour l'action: "+ ezShare.getEzName()+" nb de jours demandé: "+ nbOfDays +", nb de jours recu "+nbOfPrices);
        return null;
    }
}
