package com.pascal.ezload.service.exporter;


import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

public class EZModelChecker {
    private final Reporting reporting;

    public EZModelChecker(Reporting reporting) {
        this.reporting = reporting;
    }

    public void validateModels(List<EZModel> allEZModels) throws IOException {
        try(Reporting rep = reporting.pushSection("Checking Standard Model")){
            for (EZModel allEZModel : allEZModels) {
                validateModel(allEZModel);
            }
        }
    }

    private void validateModel(EZModel model) throws IOException {
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

    // return true if error found
    private void validateModel(EZOperation operation){

        validateAccount(operation);

        if (operation.getDesignation().isEmpty()) {
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

}
