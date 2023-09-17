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
package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.exporter.ezEdition.*;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EZPortfolioProxy {

    int getEzPortfolioVersion();

    void load(Reporting reporting) throws Exception;

    // return the list of EzEdition operation not saved
    List<EzReport> save(EzProfil profil, Reporting reporting, List<EzReport> operations, List<String> ignoreOperations) throws Exception;

    Optional<EZDate> getLastOperationDate(EnumEZBroker courtier, EZAccountDeclaration account);

    boolean isFileAlreadyLoaded(EnumEZBroker courtier, EZAccountDeclaration account, EZDate pdfDate);

    boolean isOperationsExists(Row operation);

    void applyOnPortefeuille(EzPortefeuilleEdition ezPortefeuilleEdition);

    void fillFromMonPortefeuille(EzData data, String valeur, String accountType, EnumEZBroker broker);

    Set<ShareValue> getShareValuesFromMonPortefeuille();

    EZPortfolioProxy createDeepCopy();

    Optional<EzPortefeuilleEdition> createNoOpEdition(ShareValue share);

    String getEzLiquidityName(String ezAccountType, EnumEZBroker broker);


    void applyOnPerformance(EzPerformanceEdition ezPerformanceEdition);

    MesOperations getAllOperations();
}
