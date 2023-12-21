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
    VersementFond("Versement fond");

    private String title;

    OperationTitle(String title){
        this.title = title;
    }

    public static OperationTitle build(String title) {
        Optional<OperationTitle> found = Arrays.stream(values()).filter(op -> op.getTitle().equals(title)).findFirst();
        return found.orElse(null);
    }

    public String getTitle() {
        return title;
    }
}
