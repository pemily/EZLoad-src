package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.service.parsers.bourseDirect.*;
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

    public List<String> getErrors(){
        try(Reporting ignored = reporting.pushSection("Vérification du Model extrait du PDF de BourseDirect...")){

            long nbOfDroitDeGarde = nbOfDroitsDeGarde(model.getOperations());

            if (nbOfDroitDeGarde > 1) {
                error("Le nombre de sections detecté de 'Droit de Garde' est: " + nbOfDroitDeGarde+". Une seule est attendue!");
            }

            if (nbOfDroitDeGarde == 1 && !(model.getOperations().get(model.getOperations().size() - 1) instanceof DroitsDeGarde)) {
                // si ce n'est pas le dernier, il faudra en tenir compte pour les index des dates vs operations vs amounts
                error("La section 'Droit de Garde' n'est pas la dernière section");
            }

            if (model.getDates().size() != model.getOperations().size()) {
                error("Le nombre de dates trouvées: " + model.getDates().size() + " ne correspond pas au nombre d'opérations trouvées: " + model.getOperations().size());
            }

            if (model.getAmounts().size() != model.getOperations().size() - nbOfDroitDeGarde) {
                error("Le nombre de montant trouvés: " + model.getAmounts().size() + " ne correspond pas au au nombre d'opérations trouvées: " + model.getOperations().size());
            }

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

    private long nbOfDroitsDeGarde(List<Operation> operations){
        return operations.stream().filter(operation -> operation instanceof DroitsDeGarde).count();
    }

    private void error(String newError){
        reporting.error(newError);
        errors.add(newError);
    }

}
