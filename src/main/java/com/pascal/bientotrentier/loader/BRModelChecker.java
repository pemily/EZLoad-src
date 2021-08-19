package com.pascal.bientotrentier.loader;


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
        reporting.pushSection("Checking Standard Model");
        try {
            return allBRModels.stream().allMatch(this::isActionValid);
        }
        finally {
            reporting.popSection();
        }
    }

    private boolean isActionValid(BRModel model){
        boolean result;
        reporting.pushSection(model.getSourceFile());
        try {
            result = ModelUtils.isValidDate(model.getReportDate());
            if (!result) {
                reporting.info("Date of the report is invalid: "+model.getReportDate());
            }

            result |= model.getOperations().stream().allMatch(this::isActionValid);
        }
        finally {
            reporting.popSection();
        }
        return result;
    }

    private boolean isActionValid(BROperation operation){
        boolean result;
        result = ModelUtils.isValidDate(operation.getDate());
        if (!result) {
            reporting.info("The date of the operation is invalid. "+operation);
        }

        result = operation.getCompteType() != null;
        if (!result) {
            reporting.info("The compte type for one operation is not set! "+operation);
        }

        result = !StringUtils.isBlank(operation.getCourtier());
        if (!result) {
            reporting.info("The courtier for one operation is not set! "+operation);
        }

        result = !StringUtils.isBlank(operation.getAmount());
        if (!result) {
            reporting.info("The amount for one operation is not set! "+operation);
        }

        if (operation instanceof IOperationWithAction){
            IOperationWithAction opWithTitre = (IOperationWithAction) operation;
            result = isActionValid(opWithTitre);
        }

        return result;
    }

    private boolean isActionValid(IOperationWithAction operation) {
        boolean result;
        BRAction action = operation.getAction();
        result = StringUtils.isBlank(action.getName());
        if (!result) {
            reporting.info("The action name for one operation is not set! "+operation);
        }

        result = StringUtils.isBlank(action.getCountry());
        if (!result) {
            reporting.info("The country action for one operation is not set! "+operation);
        }

        result = StringUtils.isBlank(action.getTicker());
        if (!result) {
            reporting.info("The ticker action for one operation is not set! "+operation);
        }

        result = StringUtils.isBlank(action.getMarketMic());
        if (!result) {
            reporting.info("The Market Mic for one operation is not set! "+operation);
        }
        return result;
    }
}
