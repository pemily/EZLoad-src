package com.pascal.ezload.service.exporter;


import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.IOperationWithAction;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EZModelChecker {
    private final Reporting reporting;

    public EZModelChecker(Reporting reporting) {
        this.reporting = reporting;
    }

    public void validateModels(List<EZModel> allEZModels) {
        try(Reporting rep = reporting.pushSection("Checking Standard Model")){
            allEZModels.forEach(this::validateModel);
        }
    }

    private void validateModel(EZModel model){
        try(Reporting rep = reporting.pushSection((reporting, lnkCreator) -> reporting.escape("Checking: ") + lnkCreator.createSourceLink(reporting, model.getSourceFile()))){
            if (model.getReportDate() == null){
                model.setError(true);
                reporting.info("La date du rapport n'a pas été trouvé");
            }
            else if (!model.getReportDate().isValid()) {
                model.setError(true);
                reporting.info("La date du rapport est invalide: "+model.getReportDate());
            }

            if (model.getOperations().size() == 0){
                model.setError(true);
                reporting.error("Aucune operation trouvée!");
            }
            else {
                for (EZOperation op : model.getOperations()) {
                    if (validateModel(op))
                        model.setError(true);
                }
            }
        }
    }

    // return true if error found
    private boolean validateModel(EZOperation operation){
        if (operation.getDate() == null){
            operation.setError(true);
            reporting.info("La date de l'opération n'a pas été trouvé.");
        }
        else if (!operation.getDate().isValid()) {
            operation.setError(true);
            reporting.info("La date de l'opération est invalide. "+operation);
        }

        if (operation.getCompteType() == null) {
            operation.setError(true);
            reporting.info("Le type de compte d'une opération n'a pas été trouvé! "+operation);
        }

        if (operation.getCourtier() == null) {
            operation.setError(true);
            reporting.info("Le courtier pour une opération n'a pas été trouvé! "+operation);
        }

        if (StringUtils.isBlank(operation.getAmount())) {
            operation.setError(true);
            reporting.info("Le montant d'une opération n'a pas été trouvé! "+operation);
        }

        if (operation instanceof IOperationWithAction){
            IOperationWithAction opWithTitre = (IOperationWithAction) operation;
            if (validateAction(opWithTitre))
                operation.setError(true);
        }

        return operation.hasError();
    }

    // return true if error found
    private boolean validateAction(IOperationWithAction operation) {
        EZAction action = operation.getAction();
        if (StringUtils.isBlank(action.getName())) {
            action.setError(true);
            reporting.error("L'action pour une opération n'a pas été trouvé! "+operation);
        }

        if (StringUtils.isBlank(action.getTicker())) {
            action.setError(true);
            reporting.error("L'action ticker pour une opération n'a pas été trouvé! "+operation);
        }

        if (action.getMarketPlace() == null) {
            action.setError(true);
            reporting.error("La Place de Marché pour une opération n'a pas été trouvé! "+operation);
        }
        return action.isError();
    }
}
