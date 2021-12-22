package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzOperationEdition;
import com.pascal.ezload.service.exporter.ezEdition.data.common.AccountData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.ReportData;
import com.pascal.ezload.service.exporter.rules.RuleDefinition;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.util.ModelUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MesOperations  {

    private static final String BIENTOT_RENTIER_OPERATION = "EZLoad %s - %s"; // EZLoad BourseDirect v1 - ACHAT COMPTANT

    private static final int DATE_COL = 0;
    private static final int COMPTE_TYPE_COL = 1;
    private static final int COURTIER_DISPLAY_NAME_COL = 2;
    private static final int QUANTITE_COL = 3;
    private static final int OPERATION_TYPE_COL = 4;
    private static final int ACTION_NAME_COL = 5;
    private static final int COUNTRY_COL = 6;
    private static final int AMOUNT_COL = 7;
    private static final int INFORMATION_COL = 8;
    private static final int ACCOUNT_DECLARED_NUMBER_COL = 9;
    private static final int AUTOMATIC_UPD_COL = 10;
    private static final int SOURCE_FILE_COL = 11;

    private final SheetValues existingOperations;
    private final List<Row> newOperations = new ArrayList<>();


    public MesOperations(SheetValues mesOperations){
        this.existingOperations = mesOperations;
    }

    public int getNbOfExistingOperations(){
        return existingOperations.getValues().size();
    }

    public List<Row> getNewOperations(){
        return newOperations;
    }

    public boolean isOperationsExists(Row operation){
        return existingOperations.getValues().stream().filter(
            row ->
                 operation.valueDate(DATE_COL).equals(row.valueDate(DATE_COL))
//                && operation.getValueStr(AMOUNT_COL).equals(row.getValueStr(AMOUNT_COL))
//                && operation.getValueStr(QUANTITE_COL).equals(row.getValueStr(QUANTITE_COL))
                && operation.getValueStr(COURTIER_DISPLAY_NAME_COL).equals(row.getValueStr(COURTIER_DISPLAY_NAME_COL))
//                && operation.getValueStr(COMPTE_TYPE_COL).equals(row.getValueStr(COMPTE_TYPE_COL))
                && operation.getValueStr(ACCOUNT_DECLARED_NUMBER_COL).equals(row.getValueStr(ACCOUNT_DECLARED_NUMBER_COL))
                && operation.getValueStr(SOURCE_FILE_COL).equals(row.getValueStr(SOURCE_FILE_COL))
//                && operation.getValueStr(INFORMATION_COL).equals(row.getValueStr(INFORMATION_COL))
//                && operation.getValueStr(OPERATION_TYPE_COL).equals(row.getValueStr(OPERATION_TYPE_COL))
//                && operation.getValueStr(COUNTRY_COL).equals(row.getValueStr(COUNTRY_COL))
//                && operation.getValueStr(ACTION_NAME_COL).equals(row.getValueStr(ACTION_NAME_COL))
        ).count() > 1;
    }

    public static Row newOperationRow(EzData ezData, EzOperationEdition operationEdition, RuleDefinitionSummary ruleDef) {
        Row r = new Row();
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
        newOperations.add(newOperationRow(ezData, operationEdition, ruleDef));
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
        return Optional.of(latestRow.getValueStr(SOURCE_FILE_COL)).map(ModelUtils::getDateFromFile);
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
