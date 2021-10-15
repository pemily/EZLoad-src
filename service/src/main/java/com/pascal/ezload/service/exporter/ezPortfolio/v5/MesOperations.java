package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.ezEdition.EzOperationEdition;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MesOperations  {

    private final SheetValues existingOperations;
    private final List<Row> newOperations = new ArrayList<>();
    private int firstFreeRow = -1;
    private final Reporting reporting;

    private static final String BIENTOT_RENTIER_OPERATION = "[EZLoad]";

    private static final int DATE_COL = 0;
    private static final int COMPTE_TYPE_COL = 1;
    private static final int COURTIER_DISPLAY_NAME_COL = 2;
    private static final int QUANTITE_COL = 3;
    private static final int OPERATION_TYPE_COL = 4;
    private static final int ACTION_NAME_COL = 5;
    private static final int COUNTRY_COL = 6;
    private static final int AMOUNT_COL = 7;
    private static final int INFORMATION_COL = 8;
    private static final int ACCOUNT_DECLARED_NAME_COL = 9;
    private static final int AUTOMATIC_UPD_COL = 10;


    public MesOperations(Reporting reporting, SheetValues mesOperations){
        this.reporting = reporting;
        this.existingOperations = mesOperations;
        firstFreeRow = mesOperations.getValues().size()+1;
    }

    public int getFirstFreeRow(){
        return firstFreeRow;
    }

    public List<Row> getNewOperations(){
        return newOperations;
    }

    public boolean isOperationsExists(Row operation){
        return existingOperations.getValues().stream().filter(
            row ->
                BIENTOT_RENTIER_OPERATION.equals(row.getValueStr(AUTOMATIC_UPD_COL))
                && operation.valueDate(DATE_COL).equals(row.valueDate(DATE_COL))
                && operation.getValueStr(AMOUNT_COL).equals(row.getValueStr(AMOUNT_COL))
                && operation.getValueStr(QUANTITE_COL).equals(row.getValueStr(QUANTITE_COL))
                && operation.getValueStr(COURTIER_DISPLAY_NAME_COL).equals(row.getValueStr(COURTIER_DISPLAY_NAME_COL))
                && operation.getValueStr(COMPTE_TYPE_COL).equals(row.getValueStr(COMPTE_TYPE_COL))
                && operation.getValueStr(ACCOUNT_DECLARED_NAME_COL).equals(row.getValueStr(ACCOUNT_DECLARED_NAME_COL))
                && operation.getValueStr(INFORMATION_COL).equals(row.getValueStr(INFORMATION_COL))
                && operation.getValueStr(OPERATION_TYPE_COL).equals(row.getValueStr(OPERATION_TYPE_COL))
                && operation.getValueStr(COUNTRY_COL).equals(row.getValueStr(COUNTRY_COL))
                && operation.getValueStr(ACTION_NAME_COL).equals(row.getValueStr(ACTION_NAME_COL))
        ).count() > 1;
    }

    public static Row newOperationRow(EzOperationEdition operationEdition) {
        return new Row(operationEdition.getDate(), operationEdition.getCompteType(), operationEdition.getBroker(),
                        null, operationEdition.getOperationType(), operationEdition.getActionName(),
                        operationEdition.getCountry(), operationEdition.getAmount(), operationEdition.getDescription(),
                        operationEdition.getAccount(), BIENTOT_RENTIER_OPERATION);
    }

    public void newOperation(EzOperationEdition operationEdition){
        newOperations.add(newOperationRow(operationEdition));
    }

    public boolean isFileAlreadyLoaded(EnumEZBroker courtier, EZAccountDeclaration EZAccountDeclaration, EZDate fileDate) {
        return getLastOperationDate(courtier, EZAccountDeclaration).map(lastDate -> lastDate.isBeforeOrEquals(fileDate)).orElse(false);
    }

    public Optional<EZDate> getLastOperationDate(EnumEZBroker courtier, EZAccountDeclaration EZAccountDeclaration) {
        List<Row> courtierOps = existingOperations.getValues().stream()
                .filter(row -> BIENTOT_RENTIER_OPERATION.equals(row.getValueStr(AUTOMATIC_UPD_COL))
                        && courtier.getEzPortfolioName().equals(row.getValueStr(COURTIER_DISPLAY_NAME_COL))
                        && EZAccountDeclaration.getName().equals(row.getValueStr(ACCOUNT_DECLARED_NAME_COL)))
                .collect(Collectors.toList());
        if (courtierOps.isEmpty()) return Optional.empty();
        Row latestRow = courtierOps.get(courtierOps.size()-1);
        return Optional.of(latestRow.valueDate(DATE_COL));
    }

}