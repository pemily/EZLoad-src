package com.pascal.ezload.service.exporter.ezEdition.data;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.OperationData;

public interface BourseDirectV1Data extends OperationData {

    String operation_type="operation.type";
    String operation_cours="operation.cours"; // cours
    String operation_montantBrut="operation.montantBrut"; // montant brut
    String operation_fraisCourtage="operation.fraisCourtage"; // frais de Courtage
    String operation_tva="operation.TVA"; // tva
    String operation_prixUnitBrut="operation.prixUnitBrut"; //
    String operation_commission="operation.commission";
    String operation_prelevement="operation.prélèvement";
    String operation_creditImpot="operation.créditImpot";
    String operation_contributionSocial="operation.contributionSocial";
    String operation_codeDevise="operation.devise.code";
    String operation_symbolDevise="operation.devise.symbole";

    void fill(EzData data);
}
