package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface OperationData {

    // common info in all operations
    EzDataKey operation_type = new EzDataKey("operation.type", "Le type d'opération venant du relevé d'information");
    EzDataKey operation_date = new EzDataKey("operation.date");
    EzDataKey operation_amount = new EzDataKey("operation.montant");
    EzDataKey operation_description= new EzDataKey("operation.description");
    EzDataKey operation_accountType = new EzDataKey("operation.typeCompte");

    // specific info depends of the operation type
    EzDataKey operation_quantity= new EzDataKey("operation.quantite");
    EzDataKey operation_cours= new EzDataKey("operation.cours"); // cours
    EzDataKey operation_montantBrut= new EzDataKey("operation.montantBrut"); // montant brut
    EzDataKey operation_fraisCourtage= new EzDataKey("operation.fraisCourtage"); // frais de Courtage
    EzDataKey operation_tva= new EzDataKey("operation.TVA"); // tva
    EzDataKey operation_prixUnitBrut= new EzDataKey("operation.prixUnitBrut");
    EzDataKey operation_commission= new EzDataKey("operation.commission");
    EzDataKey operation_prelevement= new EzDataKey("operation.prélèvement");
    EzDataKey operation_creditImpot= new EzDataKey("operation.créditImpot");
    EzDataKey operation_contributionSocial= new EzDataKey("operation.contributionSocial");
    EzDataKey operation_codeDevise= new EzDataKey("operation.devise.code");
    EzDataKey operation_symbolDevise= new EzDataKey("operation.devise.symbole");

    void fill(EzData data);
}
