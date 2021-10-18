package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface EzLoadOperationEditionData {
    String ezLoad_operation_date = "ezload.opération.date";
    String ezLoad_operation_accountType = "ezload.opération.typeCompte";
    String ezLoad_operation_broker = "ezload.opération.courtier";
    String ezLoad_operation_account = "ezload.opération.compte";
    String ezLoad_operation_quantity = "ezload.opération.quantité";
    String ezLoad_operation_operationType = "ezload.opération.typeOpération";
    String ezLoad_operation_shareName = "ezload.opération.valeurNom";
    String ezLoad_operation_country = "ezload.opération.pays";
    String ezLoad_operation_amount = "ezload.opération.montant";
    String ezLoad_operation_description = "ezload.opération.description";

    void fill(EzData data);
}
