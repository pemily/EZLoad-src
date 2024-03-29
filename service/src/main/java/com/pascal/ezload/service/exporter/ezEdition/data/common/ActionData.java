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

public interface ActionData {
    EzDataKey share_isin = new EzDataKey("ezOperationISIN", "le code isin de la valeur, trouvé grâce à: https://www.boursedirect.fr/api/search/");
    EzDataKey share_ezName = new EzDataKey("ezOperationShareUserName", "Le nom choisi par l'utilisateur de la valeur");
    EzDataKey share_ezCode = new EzDataKey("ezShareCode", "Le ticker Google Finance de la valeur, construit avec ezMarketGoogleCode:ezOperationTicker");
    EzDataKey share_costPrice = new EzDataKey("ezPRU", "Le PRU calculé dans l'onglet PRU de ezPortfolio");
    EzDataKey share_type = new EzDataKey("ezShareType", "Le type d'action: stock");
    EzDataKey share_countryCode = new EzDataKey("ezShareCountryCode", "Le code pays de l'action");
    EzDataKey share_country = new EzDataKey("ezShareCountry", "Le pays de l'action");
    EzDataKey share_industry = new EzDataKey("ezShareIndustry", "L'industrie de l'action");
    void fill(EzData data);
}
