package com.pascal.bientotrentier.exporter.ezPortfolio;

import com.pascal.bientotrentier.gdrive.Row;
import com.pascal.bientotrentier.gdrive.SheetValues;
import com.pascal.bientotrentier.model.BRAction;
import com.pascal.bientotrentier.model.BROperation;
import com.pascal.bientotrentier.model.BROperationType;
import com.pascal.bientotrentier.model.IOperationWithAction;
import com.pascal.bientotrentier.sources.Reporting;

import java.util.ArrayList;
import java.util.List;

public class MesOperations  {

    private final SheetValues existingOperations;
    private final List<Row> newOperations = new ArrayList<>();
    private int firstFreeRow = -1;
    private final Reporting reporting;

    private static final String BIENTOT_RENTIER_OPERATION = "[AUTO]";

    private static final int DATE_COL = 0;
    private static final int COMPTE_TYPE_COL = 1;
    private static final int COURTIER_COL = 2;
    private static final int PERIODE_COL = 3;
    private static final int OPERATION_TYPE_COL = 4;
    private static final int ACTION_NAME_COL = 5;
    private static final int COUNTRY_COL = 6;
    private static final int AMOUNT_COL = 7;
    private static final int INFORMATION_COL = 8;
    private static final int AUTOMATIC_UPD_COL = 9;


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
                        && operation.getDate().equals(row.valueStr(DATE_COL))
                        && operation.getAmount().equals(row.valueStr(AMOUNT_COL))
                        && operation.getCourtier().equals(row.valueStr(COURTIER_COL))
                        && operation.getCompteType().getEZPortfolioName().equals(row.valueStr(COMPTE_TYPE_COL))
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

    public void newOperation(String date, BROperation.COMPTE_TYPE compteType, String courtier, String periode, BROperationType operationType, String actionName, String country, String amount, String description) {
        newOperations.add(new Row(date, compteType.getEZPortfolioName(), courtier, format(periode),
                        operationType.getEZPortfolioName(), format(actionName), format(country), format(amount), format(description), BIENTOT_RENTIER_OPERATION));
    }

    public String format(String value){
        return value == null ? "" : value.replace('\n', ' ');
    }
}
