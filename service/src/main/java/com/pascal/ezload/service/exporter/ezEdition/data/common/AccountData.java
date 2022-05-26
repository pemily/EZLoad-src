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

public interface AccountData {

    EzDataKey account_name = new EzDataKey("ezAccountName", "Viens du site BourseDirect");
    EzDataKey account_number = new EzDataKey("ezAccountNumber");
    EzDataKey account_type = new EzDataKey("ezAccountType");
    EzDataKey account_owner_name = new EzDataKey("ezAccountOwnerName", "Le nom du propriétaire du compte");
    EzDataKey account_owner_address = new EzDataKey("ezAccountOwnerAddress", "L'adresse du propriétaire du compte");
    EzDataKey account_devise_symbol = new EzDataKey("ezAccountCurrencySymbol", "Le symbol de la devise du compte");
    EzDataKey account_devise_code = new EzDataKey("ezAccountCurrencyCode", "Le code de la devise du compte");

    void fill(EzData data);
}
