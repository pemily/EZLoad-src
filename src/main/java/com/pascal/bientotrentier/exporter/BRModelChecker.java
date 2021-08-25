package com.pascal.bientotrentier.exporter;


import com.pascal.bientotrentier.model.BRAction;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.model.BROperation;
import com.pascal.bientotrentier.model.IOperationWithAction;
import com.pascal.bientotrentier.sources.Reporting;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BRModelChecker {
    private final Reporting reporting;

    public BRModelChecker(Reporting reporting) {
        this.reporting = reporting;
    }

    public boolean generateReport(List<BRModel> allBRModels) {
        try(Reporting rep = reporting.pushSection("Checking Standard Model")){
            boolean allActionsValid = true;
            for (BRModel model : allBRModels){
                if (!generateReport(model))
                    allActionsValid = false;
            }
            return allActionsValid;
        }
    }

    private boolean generateReport(BRModel model){
        boolean isValid = true;
        try(Reporting rep = reporting.pushSection((reporting, lnkCreator) -> reporting.escape("Checking: ") + lnkCreator.createSourceLink(reporting, model.getSourceFile()))){
            if (!model.getReportDate().isValid()) {
                isValid = false;
                reporting.info("Date of the report is invalid: "+model.getReportDate());
            }

            for (BROperation op : model.getOperations()) {
                if (!generateReport(op))
                    isValid = false;
            }
        }
        return isValid;
    }

    private boolean generateReport(BROperation operation){
        boolean isValid = true;
        if (!operation.getDate().isValid()) {
            isValid = false;
            reporting.info("The date of the operation is invalid. "+operation);
        }

        if (operation.getCompteType() == null) {
            isValid = false;
            reporting.info("The compte type for one operation is not set! "+operation);
        }

        if (operation.getCourtier() == null) {
            isValid = false;
            reporting.info("The courtier for one operation is not set! "+operation);
        }

        if (StringUtils.isBlank(operation.getAmount())) {
            isValid = false;
            reporting.info("The amount for one operation is not set! "+operation);
        }

        if (operation instanceof IOperationWithAction){
            IOperationWithAction opWithTitre = (IOperationWithAction) operation;
            if (!generateReport(opWithTitre))
                isValid = false;
        }

        return isValid;
    }

    private boolean generateReport(IOperationWithAction operation) {
        boolean isValid = true;
        BRAction action = operation.getAction();
        if (StringUtils.isBlank(action.getName())) {
            isValid = false;
            reporting.error("The action name for one operation is not set! "+operation);
        }

        if (StringUtils.isBlank(action.getTicker())) {
            isValid = false;
            reporting.error("The ticker action for one operation is not set! "+operation);
        }

        if (action.getMarketPlace() == null) {
            isValid = false;
            reporting.error("The Market Place for one operation is not set! "+operation);
        }
        return isValid;
    }
}
