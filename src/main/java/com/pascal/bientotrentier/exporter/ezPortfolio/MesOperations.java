package com.pascal.bientotrentier.exporter.ezPortfolio;

import com.pascal.bientotrentier.model.BRAction;
import com.pascal.bientotrentier.model.BROperation;
import com.pascal.bientotrentier.model.BROperationType;
import com.pascal.bientotrentier.model.IOperationWithAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class MesOperations {

    private List<List<Object>> existingOperations;
    private List<List<Object>> newOperations = new ArrayList<>();
    private int firstFreeRow = -1;

    private static String BIENTOT_RENTIER_OPERATION = "[AUTO]";

    private static int DATE_COL = 0;
    private static int COMPTE_TYPE_COL = 1;
    private static int COURTIER_COL = 2;
    private static int PERIODE_COL = 3;
    private static int OPERATION_TYPE_COL = 4;
    private static int ACTION_NAME_COL = 5;
    private static int COUNTRY_COL = 6;
    private static int AMOUNT_COL = 7;
    private static int INFORMATION_COL = 8;
    private static int AUTOMATIC_UPD_COL = 9;


    public MesOperations(List<List<Object>> mesOperations){
        this.existingOperations = mesOperations;
        firstFreeRow = mesOperations.size()+1;
    }

    public int getFirstFreeRow(){
        return firstFreeRow;
    }

    public List<List<Object>> getNewOperations(){
        return newOperations;
    }

    public boolean isOperationsExists(BROperation operation){
        return getRowByFilter(
            row -> {
                boolean opResult =
                        BIENTOT_RENTIER_OPERATION.equals(row.value(AUTOMATIC_UPD_COL))
                        && operation.getDate().equals(row.value(DATE_COL))
                        && operation.getAmount().equals(row.value(AMOUNT_COL))
                        && operation.getCourtier().equals(row.value(COURTIER_COL))
                        && operation.getCompteType().getEZPortfolioName().equals(row.value(COMPTE_TYPE_COL))
                        && operation.getDescription().equals(row.value(INFORMATION_COL))
                        && operation.getOperationType().getEZPortfolioName().equals(row.value(OPERATION_TYPE_COL));

                if (operation instanceof IOperationWithAction){
                    IOperationWithAction opWithAction = (IOperationWithAction) operation;
                    BRAction action = opWithAction.getAction();
                    opResult &=
                            action.getCountry().equals(row.value(COUNTRY_COL))
                            && action.getName().equals(row.value(ACTION_NAME_COL));
                }
                return opResult;
            }
        ).count() == 1;
    }

    private Stream<List<Object>> getRowByFilter(Function<Row, Boolean> rowFilter){
        return existingOperations.stream().filter(r -> rowFilter.apply(new Row(r)));
    }

    public void newOperation(String date, BROperation.COMPTE_TYPE compteType, String courtier, String periode, BROperationType operationType, String actionName, String country, String amount, String description) {
        newOperations.add(Arrays.asList(date, compteType.getEZPortfolioName(), courtier, format(periode),
                        operationType.getEZPortfolioName(), format(actionName), format(country), format(amount), format(description), BIENTOT_RENTIER_OPERATION));
    }

    public String format(String value){
        return value == null ? "" : value.replace('\n', ' ');
    }
}
