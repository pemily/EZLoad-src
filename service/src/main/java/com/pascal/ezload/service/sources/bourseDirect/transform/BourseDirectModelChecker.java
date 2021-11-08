package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class BourseDirectModelChecker {
    private final Reporting reporting;
    private final BourseDirectModel model;
    private final List<String> errors = new LinkedList<>();

    public BourseDirectModelChecker(Reporting reporting, BourseDirectModel model){
        this.reporting = reporting;
        this.model = model;
    }

    public List<String> searchErrors(){
        try(Reporting ignored = reporting.pushSection("Vérification du Model extrait du PDF de BourseDirect...")){

            if (!"€".equals(model.getDeviseCredit())) {
                error("La devise de la colonne Crédit n'est pas en € mais en: " + model.getDeviseCredit());
            }

            if (!"€".equals(model.getDeviseDebit())) {
                error("La devise de la colonne Débit n'est pas en € mais en: " + model.getDeviseDebit());
            }

            if (StringUtils.isBlank(model.getAccountNumber())) {
                error("Le numéro de compte est vide");
            }

            if (StringUtils.isBlank(model.getAccountOwnerName())) {
                error("Le nom du compte est vide");
            }

            if (StringUtils.isBlank(model.getAddress())) {
                error("L'adresse du compte est vide");
            }

            if (StringUtils.isBlank(model.getAccountType())) {
                error("Le type du compte est vide");
            }

            if (model.getDateAvisOperation() == null) {
                error("La date du relevée d'opérations est vide");
            }

            if (errors.size() == 0) {
                reporting.info("=> Pas d'erreur détecté");
            }

            return errors;
        }
    }

    private void error(String newError){
        reporting.error(newError);
        errors.add(newError);
    }

}
