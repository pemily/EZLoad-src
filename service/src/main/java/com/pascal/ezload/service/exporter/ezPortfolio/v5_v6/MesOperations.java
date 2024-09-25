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
package com.pascal.ezload.service.exporter.ezPortfolio.v5_v6;

import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzOperationEdition;
import com.pascal.ezload.service.exporter.ezEdition.data.common.AccountData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.ReportData;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MesOperations  {
    private static final String BIENTOT_RENTIER_OPERATION = "EZLoad %s - %s"; // EZLoad BourseDirect v1 - ACHAT COMPTANT

    public static final int DATE_COL = 0;
    public static final int COMPTE_TYPE_COL = 1;
    public static final int COURTIER_DISPLAY_NAME_COL = 2;
    public static final int QUANTITE_COL = 3;
    public static final int OPERATION_TYPE_COL = 4;
    public static final int ACTION_NAME_COL = 5;
    public static final int COUNTRY_COL = 6;
    public static final int AMOUNT_COL = 7;
    public static final int INFORMATION_COL = 8;
    public static final int ACCOUNT_DECLARED_NUMBER_COL = 9;
    public static final int AUTOMATIC_UPD_COL = 10;
    public static final int SOURCE_FILE_COL = 11;

    private final SheetValues existingOperations;
    private final List<Row> newOperations = new ArrayList<>();


    public MesOperations(SheetValues mesOperations){
        this.existingOperations = mesOperations;
    }

    public int getNbOfExistingOperations(){
        return existingOperations.getValues().size();
    }

    public List<Row> getExistingOperations(){
        return existingOperations.getValues();
    }

    public List<Row> getNewOperations(){
        return newOperations;
    }

    public boolean isOperationsExists(Row operation){
        return existingOperations.getValues().stream().anyMatch(row ->
                operation.getValueDate(DATE_COL).equals(row.getValueDate(DATE_COL))
                && operation.getValueStr(COURTIER_DISPLAY_NAME_COL).equals(row.getValueStr(COURTIER_DISPLAY_NAME_COL))
                && operation.getValueStr(ACCOUNT_DECLARED_NUMBER_COL).equals(row.getValueStr(ACCOUNT_DECLARED_NUMBER_COL))
                && operation.getValueStr(SOURCE_FILE_COL).equals(row.getValueStr(SOURCE_FILE_COL)));
    }

    public static Row newOperationRow(int rowNumber, EzData ezData, EzOperationEdition operationEdition, RuleDefinitionSummary ruleDef) {
        Row r = new Row(rowNumber);
        r.setValue(DATE_COL, operationEdition.getDate());
        r.setValue(COMPTE_TYPE_COL, operationEdition.getAccountType());

        r.setValue(COURTIER_DISPLAY_NAME_COL, operationEdition.getBroker());
        r.setValue(QUANTITE_COL, operationEdition.getQuantity());
        r.setValue(OPERATION_TYPE_COL, operationEdition.getOperationType());
        r.setValue(ACTION_NAME_COL,operationEdition.getShareName());
        r.setValue(COUNTRY_COL, operationEdition.getCountry());
        r.setValue(AMOUNT_COL, operationEdition.getAmount());
        r.setValue(INFORMATION_COL, operationEdition.getDescription());
        r.setValue(ACCOUNT_DECLARED_NUMBER_COL, ezData.get(AccountData.account_number));
        r.setValue(AUTOMATIC_UPD_COL, String.format(BIENTOT_RENTIER_OPERATION,
                                ruleDef.getBroker() == null ? "" : ruleDef.getBroker()+ // can be null when we set the startDate in the config panel or the operation is ignored
                                        (ruleDef.getBrokerFileVersion() == -1 ? "" : " v"+ruleDef.getBrokerFileVersion()),  // can be -1 when we set the startDate in the config panel
                                ruleDef.getName()));
        r.setValue(SOURCE_FILE_COL, ezData.get(ReportData.report_source));
        return r;
    }

    public void newOperation(EzData ezData, EzOperationEdition operationEdition, RuleDefinitionSummary ruleDef){
        newOperations.add(newOperationRow(EZPorfolioProxyV5_V6.FIRST_ROW_MES_OPERATIONS+existingOperations.getValues().size()+newOperations.size(), ezData, operationEdition, ruleDef));
    }

    public boolean isFileAlreadyLoaded(EnumEZBroker courtier, EZAccountDeclaration EZAccountDeclaration, EZDate fileDate) {
        return getLastOperationDate(courtier, EZAccountDeclaration).map(fileDate::isBeforeOrEquals).orElse(false);
    }

    public Optional<EZDate> getLastOperationDate(EnumEZBroker courtier, EZAccountDeclaration ezAccountDeclaration) {
        List<Row> courtierOps = existingOperations.getValues().stream()
                .filter(row -> courtier.getEzPortfolioName().equals(row.getValueStr(COURTIER_DISPLAY_NAME_COL))
                        && ezAccountDeclaration.getNumber().equals(row.getValueStr(ACCOUNT_DECLARED_NUMBER_COL)))
                .collect(Collectors.toList());
        if (courtierOps.isEmpty()) return Optional.empty();
        Row latestRow = courtierOps.get(courtierOps.size()-1);
        // the file can contains a very old date (pour une opération de régulatisation par exemple)
        // donc je ne prends pas la colonne date, mais je déduis la date a partir du fichier stocké dans ezPortfolio
        // mais si je n'ai pas de fichier, (pour une initialisation de Date de Départ par exemple) dans ce cas je prends la colonne date
        if (latestRow.getValueStr(SOURCE_FILE_COL).equals("")){
            return Optional.ofNullable(latestRow.getValueDate(DATE_COL));
        }
        return Optional.of(latestRow.getValueStr(SOURCE_FILE_COL)).map(BourseDirectDownloader::getDateFromFilePath);
    }

    public void saveDone() {
        existingOperations.getValues().addAll(newOperations);
        newOperations.clear();
    }

    public MesOperations createDeepCopy() {
        MesOperations copy = new MesOperations(existingOperations.createDeepCopy());
        copy.newOperations.addAll(newOperations.stream().map(Row::createDeepCopy).collect(Collectors.toList()));
        return copy;
    }
}
