package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface OperationData {

    // common info in all operations
    EzDataKey operation_type = new EzDataKey("ezOperationType", "Le type d'opération venant du relevé d'information");
    EzDataKey operation_date = new EzDataKey("ezOperationDate", "la date de l'opération");
    EzDataKey operation_amount = new EzDataKey("ezOperationAmount", "Le montant de l'opération");
    EzDataKey operation_description= new EzDataKey("ezOperationDescription", "la description de l'opération");

    // specific info depends of the operation type
    EzDataKey operation_quantity= new EzDataKey("ezOperationQuantity");
    EzDataKey operation_cours= new EzDataKey("ezOperationPrice", "Le cours de la valeur"); // cours
    EzDataKey operation_montantBrut= new EzDataKey("ezOperationGrossAmount", "Le montant Brut"); // montant brut
    EzDataKey operation_fraisCourtage= new EzDataKey("ezOperationBrokerageFees", "Les frais de courage"); // frais de Courtage
    EzDataKey operation_tva= new EzDataKey("ezOperationVAT", "La TVA"); // tva
    EzDataKey operation_prixUnitBrut= new EzDataKey("ezOperationGrossUnitPrice", "Le prix unitaire brut");
    EzDataKey operation_commission= new EzDataKey("ezOperationCommission", "La commission");
    EzDataKey operation_prelevement= new EzDataKey("ezOperationDeduction", "Les prélèvements");
    EzDataKey operation_creditImpot= new EzDataKey("ezOperationTaxCredit", "Le crédit d'impôt");
    EzDataKey operation_contributionSocial= new EzDataKey("ezOperationSocialContribution", "La contribution sociale");
    EzDataKey operation_codeDevise= new EzDataKey("ezOperationCurrencyCode", "Le code de la devise de l'opération");
    EzDataKey operation_symbolDevise= new EzDataKey("ezOperationCurrencySymbol", "le symbol de la devise de l'opération");

    void fill(EzData data);
}
