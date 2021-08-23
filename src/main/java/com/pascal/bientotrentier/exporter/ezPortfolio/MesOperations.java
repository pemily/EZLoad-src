package com.pascal.bientotrentier.exporter.ezPortfolio;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.gdrive.Row;
import com.pascal.bientotrentier.gdrive.SheetValues;
import com.pascal.bientotrentier.model.*;
import com.pascal.bientotrentier.sources.Reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MesOperations  {

    private final SheetValues existingOperations;
    private final List<Row> newOperations = new ArrayList<>();
    private int firstFreeRow = -1;
    private final Reporting reporting;

    private static final String BIENTOT_RENTIER_OPERATION = "[AUTO]";

    private static final int DATE_COL = 0;
    private static final int COMPTE_TYPE_COL = 1;
    private static final int COURTIER_DISPLAY_NAME_COL = 2;
    private static final int PERIODE_COL = 3;
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

    public boolean isOperationsExists(BROperation operation){
        return existingOperations.getValues().stream().filter(
            row -> {
                boolean opResult =
                        BIENTOT_RENTIER_OPERATION.equals(row.valueStr(AUTOMATIC_UPD_COL))
                        && operation.getDate().equals(row.valueDate(DATE_COL))
                        && operation.getAmount().equals(row.valueStr(AMOUNT_COL))
                        && operation.getCourtier().getDisplayName().equals(row.valueStr(COURTIER_DISPLAY_NAME_COL))
                        && operation.getCompteType().getEZPortfolioName().equals(row.valueStr(COMPTE_TYPE_COL))
                        && operation.getAccountDeclaration().getName().equals(row.valueStr(ACCOUNT_DECLARED_NAME_COL))
                        && operation.getDescription().equals(row.valueStr(INFORMATION_COL))
                        && operation.getOperationType().getEZPortfolioName().equals(row.valueStr(OPERATION_TYPE_COL));

                if (operation instanceof IOperationWithAction){
                    IOperationWithAction opWithAction = (IOperationWithAction) operation;
                    BRAction action = opWithAction.getAction();
                    opResult &=
                            action.getMarketPlace().getCountry().getName().equals(row.valueStr(COUNTRY_COL))
                            && action.getName().equals(row.valueStr(ACTION_NAME_COL));
                }
                return opResult;
            }
        ).count() == 1;
    }

    public void newOperation(BRDate date, EnumBRCompteType compteType, EnumBRCourtier courtier, MainSettings.AccountDeclaration account, String periode, BROperationType operationType, String actionName, String country, String amount, String description) {
        newOperations.add(new Row(date.toEzPortoflioDate(), compteType.getEZPortfolioName(), courtier.getDisplayName(), format(periode),
                        operationType.getEZPortfolioName(), format(actionName), format(country), format(amount), format(description), format(account.getName()), BIENTOT_RENTIER_OPERATION));
    }

    public String format(String value){
        return value == null ? "" : value.replace('\n', ' ');
    }


    public boolean isAlreadyProcessed(EnumBRCourtier courtier, MainSettings.AccountDeclaration accountDeclaration, BRDate pdfDate) {
        List<Row> courtierOps = existingOperations.getValues().stream()
                .filter(row -> BIENTOT_RENTIER_OPERATION.equals(row.valueStr(AUTOMATIC_UPD_COL))
                        && courtier.getDisplayName().equals(row.valueStr(COURTIER_DISPLAY_NAME_COL))
                        && accountDeclaration.getName().equals(row.valueStr(ACCOUNT_DECLARED_NAME_COL)))
                .collect(Collectors.toList());

        if (courtierOps.isEmpty()) return false;
        Row latestRow = courtierOps.get(courtierOps.size()-1);
        BRDate lastOperationDate = latestRow.valueDate(DATE_COL);
        return lastOperationDate.isBeforeOrEquals(pdfDate);
    }

}
