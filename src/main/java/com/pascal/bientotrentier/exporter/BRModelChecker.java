package com.pascal.bientotrentier.exporter;


import com.pascal.bientotrentier.model.BRAction;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.model.BROperation;
import com.pascal.bientotrentier.model.IOperationWithAction;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.util.ModelUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BRModelChecker {
    private Reporting reporting;

    public BRModelChecker(Reporting reporting) {
        this.reporting = reporting;
    }

    public boolean isActionValid(List<BRModel> allBRModels) {
        try(Reporting rep = reporting.pushSection("Checking Standard Model")){
            return allBRModels.stream().allMatch(this::isActionValid);
        }
    }

    private boolean isActionValid(BRModel model){
        boolean isValid;
        try(Reporting rep = reporting.pushSection("Checking: "+model.getSourceFile())){
            isValid = model.getReportDate().isValid();
            if (!isValid) {
                reporting.info("Date of the report is invalid: "+model.getReportDate());
            }

            isValid |= model.getOperations().stream().allMatch(this::isActionValid);
        }
        return isValid;
    }

    private boolean isActionValid(BROperation operation){
        boolean isValid;
        isValid = operation.getDate().isValid();
        if (!isValid) {
            reporting.info("The date of the operation is invalid. "+operation);
        }

        isValid = operation.getCompteType() != null;
        if (!isValid) {
            reporting.info("The compte type for one operation is not set! "+operation);
        }

        isValid = operation.getCourtier() != null;
        if (!isValid) {
            reporting.info("The courtier for one operation is not set! "+operation);
        }

        isValid = !StringUtils.isBlank(operation.getAmount());
        if (!isValid) {
            reporting.info("The amount for one operation is not set! "+operation);
        }

        if (operation instanceof IOperationWithAction){
            IOperationWithAction opWithTitre = (IOperationWithAction) operation;
            isValid = isActionValid(opWithTitre);
        }

        return isValid;
    }

    private boolean isActionValid(IOperationWithAction operation) {
        boolean isValid;
        BRAction action = operation.getAction();
        isValid = StringUtils.isBlank(action.getName());
        if (!isValid) {
            reporting.error("The action name for one operation is not set! "+operation);
        }

        isValid = StringUtils.isBlank(action.getTicker());
        if (!isValid) {
            reporting.error("The ticker action for one operation is not set! "+operation);
        }

        if (action.getMarketPlace() == null) {
            reporting.error("The Market Place for one operation is not set! "+operation);
        }
        return isValid;
    }
}
