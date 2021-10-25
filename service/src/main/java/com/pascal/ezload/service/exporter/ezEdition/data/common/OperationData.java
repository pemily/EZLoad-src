package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface OperationData {

    String operation_date="operation.date";
    String operation_amount ="operation.montant";
    String operation_description="operation.description";
    String operation_accountType ="operation.typeCompte";
    String operation_quantity="operation.quantite";

    void fill(EzData data);
}
