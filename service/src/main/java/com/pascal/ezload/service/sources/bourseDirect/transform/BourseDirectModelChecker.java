/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
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

    public List<String> searchErrors() throws IOException {
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
