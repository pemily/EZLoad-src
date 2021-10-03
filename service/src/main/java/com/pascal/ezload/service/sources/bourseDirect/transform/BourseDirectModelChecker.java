package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.service.parsers.bourseDirect.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BourseDirectModelChecker {
    private final Reporting reporting;

    public BourseDirectModelChecker(Reporting reporting){
        this.reporting = reporting;
    }

    public boolean isValid(BourseDirectModel model){
        try(Reporting ignored = reporting.pushSection("Vérification du Model extrait du PDF de BourseDirect...")){
            boolean isValid = true;

            long nbOfDroitDeGarde = nbOfDroitsDeGarde(model.getOperations());

            if (nbOfDroitDeGarde > 1) {
                reporting.error("Le nombre de sections detecté de 'Droit de Garde' est: " + nbOfDroitDeGarde+". Une seule est attendue!");
                isValid = false;
            }

            if (nbOfDroitDeGarde == 1 && !(model.getOperations().get(model.getOperations().size() - 1) instanceof DroitsDeGarde)) {
                // si ce n'est pas le dernier, il faudra en tenir compte pour les index des dates vs operations vs amounts
                reporting.error("La section 'Droit de Garde' n'est pas la dernière section");
            }

            if (model.getDates().size() != model.getOperations().size()) {
                reporting.error("Le nombre de dates trouvées: " + model.getDates().size() + " ne correspond pas au nombre d'opérations trouvées: " + model.getOperations().size());
                isValid = false;
            }

            if (model.getAmounts().size() != model.getOperations().size() - nbOfDroitDeGarde) {
                reporting.error("Le nombre de montant trouvés: " + model.getAmounts().size() + " ne correspond pas au au nombre d'opérations trouvées: " + model.getOperations().size());
                isValid = false;
            }

            if (!model.getDeviseCredit().equals("€")) {
                reporting.error("La devise de la colonne Crédit n'est pas en € mais en: " + model.getDeviseCredit());
                isValid = false;
            }

            if (!model.getDeviseDebit().equals("€")) {
                reporting.error("La devise de la colonne Débit n'est pas en € mais en: " + model.getDeviseDebit());
                isValid = false;
            }

            if (StringUtils.isBlank(model.getAccountNumber())) {
                reporting.error("Le numéro de compte est vide");
                isValid = false;
            }

            if (StringUtils.isBlank(model.getAccountOwnerName())) {
                reporting.error("Le nom du compte est vide");
                isValid = false;
            }

            if (StringUtils.isBlank(model.getAddress())) {
                reporting.error("L'adresse du compte est vide");
                isValid = false;
            }

            if (StringUtils.isBlank(model.getAccountType())) {
                reporting.error("Le type du compte est vide");
                isValid = false;
            }

            if (model.getDateAvisOperation() == null) {
                reporting.error("La date du relevée d'opérations est vide");
                isValid = false;
            }

            if (isValid) {
                reporting.info("=> Pas d'erreur détecté");
            }

            return isValid;
        }
    }

    private long nbOfDroitsDeGarde(List<Operation> operations){
        return operations.stream().filter(operation -> operation instanceof DroitsDeGarde).count();
    }


}
