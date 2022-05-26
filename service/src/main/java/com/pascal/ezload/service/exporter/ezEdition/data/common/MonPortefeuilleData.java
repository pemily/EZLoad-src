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
package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface MonPortefeuilleData {

    EzDataKey ezPortfolio_portefeuille_share = new EzDataKey("ezMonPortefeuilleValeur", "La valeur présente dans ezPortfolio.MonPortefeuille");
    EzDataKey ezPortfolio_portefeuille_account_type = new EzDataKey("ezMonPortefeuilleTypeCompte", "Le type de compte présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_broker = new EzDataKey("ezMonPortefeuilleCourtier", "Le courtier présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_googleTicker = new EzDataKey("ezMonPortefeuilleTickerGoogle", "Le ticker Google présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_country = new EzDataKey("ezMonPortefeuillePays", "Le pays présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_sector = new EzDataKey("ezMonPortefeuilleSecteur", "Le secteur présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_industry = new EzDataKey("ezMonPortefeuilleIndustrie", "L'industrie présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_deduction40 = new EzDataKey("ezMonPortefeuilleEligibiliteAbbatement40", "L'éligibilité de l'abbatement à 40% présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_type = new EzDataKey("ezMonPortefeuilleType", "Le type présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_costPriceUnit = new EzDataKey("ezMonPortefeuillePrixRevientUnitaire", "Le PRU présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_quantity = new EzDataKey("ezMonPortefeuilleQuantite", "La quantité présente dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_annualDividend = new EzDataKey("ezMonPortefeuilleDividendeAnnuel", "Le dividende annuel présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");


    void fill(EzData data, String share);
}
