package com.pascal.bientotrentier.sources.bourseDirect.transform;

import com.pascal.bientotrentier.parsers.bourseDirect.DroitsDeGarde;
import com.pascal.bientotrentier.parsers.bourseDirect.Operation;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.transform.model.BourseDirectModel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BourseDirectModelChecker {
    private final Reporting reporting;

    public BourseDirectModelChecker(Reporting reporting){
        this.reporting = reporting;
    }

    public boolean isValid(BourseDirectModel model){
        try(Reporting rep = reporting.pushSection("Checking BourseDirect model...")){
            boolean isValid = true;

            long nbOfDroitDeGarde = nbOfDroitsDeGarde(model.getOperations());

            if (nbOfDroitDeGarde > 1) {
                reporting.error("The number of 'Droit de Garde' sections detected is: " + nbOfDroitDeGarde);
                isValid = false;
            }

            if (nbOfDroitDeGarde == 1 && !(model.getOperations().get(model.getOperations().size() - 1) instanceof DroitsDeGarde)) {
                // si ce n'est pas le dernier, il faudra en tenir compte pour les index des dates vs operations vs amounts
                reporting.error("The 'Droit de Garde' section is not the latest");
            }

            if (model.getDates().size() != model.getOperations().size()) {
                reporting.error("The number of dates found: " + model.getDates().size() + " do not match the number of operations found: " + model.getOperations().size());
                isValid = false;
            }

            if (model.getAmounts().size() != model.getOperations().size() - nbOfDroitDeGarde) {
                reporting.error("The number of amounts found: " + model.getAmounts().size() + " do not match the number of operations found: " + model.getOperations().size());
                isValid = false;
            }

            if (!model.getDeviseCredit().equals("€")) {
                reporting.error("The credit devise is not € : " + model.getDeviseCredit());
                isValid = false;
            }

            if (!model.getDeviseDebit().equals("€")) {
                reporting.error("The debit devise is not € : " + model.getDeviseDebit());
                isValid = false;
            }

            if (StringUtils.isBlank(model.getAccountNumber())) {
                reporting.error("The account number is empty");
                isValid = false;
            }

            if (StringUtils.isBlank(model.getAccountOwnerName())) {
                reporting.error("The account owner is empty");
                isValid = false;
            }

            if (StringUtils.isBlank(model.getAddress())) {
                reporting.error("The owner address is empty");
                isValid = false;
            }

            if (StringUtils.isBlank(model.getAccountType())) {
                reporting.error("The account type is empty");
                isValid = false;
            }

            if (model.getDateAvisOperation() == null) {
                reporting.error("The date of the report is empty");
                isValid = false;
            }

            if (isValid) {
                reporting.info("=> No error detected");
            }

            return isValid;
        }
    }

    private long nbOfDroitsDeGarde(List<Operation> operations){
        return operations.stream().filter(operation -> operation instanceof DroitsDeGarde).count();
    }


}
