package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface EzLoadOperationEditionData {
    String ezLoad_operation_date = "ezload.operation.date";
    String ezLoad_operation_accountType = "ezload.operation.typeCompte";
    String ezLoad_operation_broker = "ezload.operation.courtier";
    String ezLoad_operation_quantity = "ezload.operation.quantite";
    String ezLoad_operation_operationType = "ezload.operation.typeoperation";
    String ezLoad_operation_shareName = "ezload.operation.valeurNom";
    String ezLoad_operation_country = "ezload.operation.pays";
    String ezLoad_operation_amount = "ezload.operation.montant";
    String ezLoad_operation_description = "ezload.operation.description";

    void fill(EzData data);
}
