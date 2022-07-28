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
package com.pascal.ezload.service.exporter.ezPortfolio.v4;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EZPorfolioProxyV4 implements EZPortfolioProxy {


    public EZPorfolioProxyV4(Reporting reporting, GDriveSheets sheets){
    }

    @Override
    public int getEzPortfolioVersion() {
        return 4;
    }

    @Override
    public void load(Reporting reporting) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public List<EzReport> save(EzProfil ezProfil, Reporting reporting, List<EzReport> operationsToAdd, List<String> ignoreEzEditionId){
        throw new NotImplementedException();
    }

    @Override
    public Optional<EZDate> getLastOperationDate(EnumEZBroker courtier, EZAccountDeclaration account) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isFileAlreadyLoaded(EnumEZBroker courtier, EZAccountDeclaration account, EZDate pdfDate) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOperationsExists(Row operation) {
        throw new NotImplementedException();
    }

    @Override
    public void applyOnPortefeuille(EzPortefeuilleEdition ezPortefeuilleEdition) {
        throw new NotImplementedException();
    }

    @Override
    public void fillFromMonPortefeuille(EzData data, String valeur, String accountType, EnumEZBroker broker) {
        throw new NotImplementedException();
    }

    @Override
    public Set<ShareValue> getShareValuesFromMonPortefeuille() {
        throw new NotImplementedException();
    }

    @Override
    public Optional<EZAction> findShareByIsin(String isin) {
        throw new NotImplementedException();
    }

    @Override
    public EZPortfolioProxy createDeepCopy(List<EZAction> newShares) {
        return null;
    }

    @Override
    public Optional<EzPortefeuilleEdition> createNoOpEdition(ShareValue ticker) {
        return Optional.empty();
    }

    @Override
    public String getEzLiquidityName(String ezAccountType, EnumEZBroker broker) {
        throw new NotImplementedException();
    }

    @Override
    public void newAction(EZAction v) {
        throw new NotImplementedException();
    }

    @Override
    public List<EZAction> getNewShares() {
        throw new NotImplementedException();
    }

    @Override
    public void updateNewShare(EZAction shareValue) {
        throw new NotImplementedException();
    }

}
