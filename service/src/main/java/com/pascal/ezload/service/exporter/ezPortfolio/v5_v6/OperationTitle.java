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
package com.pascal.ezload.service.exporter.ezPortfolio.v5_v6;

import java.util.Arrays;
import java.util.Optional;

public enum OperationTitle {

    AchatTitres("Achat titres"),
    AcompteImpotSurRevenu("Acompte Impôt sur le Revenu"),
    CourtageSurAchatDeTitres("Courtage sur achat de titres"),
    CourtageSurVenteDeTitres("Courtage sur vente de titres"),
    Divers("Divers"),
    DividendeBrut("Dividende brut"),
    DividendeBrutNonSoumisAAbattement("Dividende brut NON soumis à abattement"),
    DividendeBrutSoumisAAbattement("Dividende brut soumis à abattement"),
    DividendeVerse("Dividende versé"),
    DroitsDeGardeFraisDivers("Droits de garde/Frais divers"),
    PrelevementsSociaux("Prélèvements sociaux"),
    PrelevementsSociauxSurRetraitPEA("Prélèvements sociaux sur retrait PEA"),
    RetenueFiscale("Retenue fiscale"),
    RetraitFonds("Retrait fonds"),
    TaxeSurLesTransactions("Taxe sur les Transactions"),
    VenteTitres("Vente titres"),
    VersementFonds("Versement fonds");

    private String title;

    OperationTitle(String title){
        this.title = title;
    }

    public static OperationTitle build(String title) {
        Optional<OperationTitle> found = Arrays.stream(values()).filter(op -> op.getTitle().equals(title)).findFirst();
        return found.orElseThrow(() -> new IllegalStateException("L'opération avec le type: "+title+" n'est pas conforme a EzPortfolio"));
    }

    public String getTitle() {
        return title;
    }
}
