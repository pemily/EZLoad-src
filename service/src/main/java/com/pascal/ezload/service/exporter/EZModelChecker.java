package com.pascal.ezload.service.exporter;


import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
                addError(model, "La date du rapport n'a pas été trouvé");
            }
            else if (!model.getReportDate().isValid()) {
                addError(model, "La date du rapport est invalide: "+model.getReportDate());
            }

            if (model.getOperations().size() == 0){
                addError(model, "Aucune operation trouvée!");
            }
            else {
                model.getOperations().forEach(this::validateModel);
            }
        }
    }

    private void addError(EZModel model, String error){
        reporting.error(error);
        model.getErrors().add(error);
    }

    private void addError(EZOperation operation, String error){
        reporting.error(error);
        operation.getErrors().add(error);
    }

    private void addError(IOperationWithAction operation, String error){
        reporting.error(error);
        operation.getErrors().add(error);
    }

    // return true if error found
    private void validateModel(EZOperation operation){

        validateAccount(operation);

        if (operation.getOperationType() == null) {
            addError(operation, "Le type de l'opération n'a pas été trouvé! "+operation);
        }

        if (operation.getDate() == null){
            addError(operation, "La date de l'opération n'a pas été trouvé! "+operation);
        }
        else if (!operation.getDate().isValid()) {
            addError(operation, "La date de l'opération est invalide. "+operation);
        }

        if (operation.getBroker() == null) {
            addError(operation, "Le courtier pour une opération n'a pas été trouvé! "+operation);
        }

        if (StringUtils.isBlank(operation.getAmount())) {
            addError(operation, "Le montant d'une opération n'a pas été trouvé! "+operation);
        }

        if (operation instanceof IOperationWithAction){
            IOperationWithAction opWithTitre = (IOperationWithAction) operation;
            validateAction(opWithTitre);
        }
    }

    private void validateAccount(EZOperation operation) {
        EZAccount account = operation.getAccount();

        if (StringUtils.isBlank(account.getAccountNumber())){
            addError(operation, "Le numéro de compte n'a pas été trouvé! "+operation);
        }

        if (StringUtils.isBlank(account.getAccountType())){
            addError(operation, "Le type de compte n'a pas été trouvé! "+operation);
        }

        if (account.getDevise() == null){
            addError(operation, "La devise du compte n'a pas été trouvé! "+operation);
        }
    }

    // return true if error found
    private void validateAction(IOperationWithAction operation) {
        EZAction action = operation.getAction();
        if (StringUtils.isBlank(action.getRawName())) {
            addError(operation, "L'action pour une opération n'a pas été trouvé! "+operation);
        }

        if (StringUtils.isBlank(action.getTicker())) {
            addError(operation,"L'action ticker pour une opération n'a pas été trouvé! "+operation);
        }

        if (action.getMarketPlace() == null) {
            addError(operation,"La Place de Marché pour une opération n'a pas été trouvé! "+operation);
        }
    }
}
