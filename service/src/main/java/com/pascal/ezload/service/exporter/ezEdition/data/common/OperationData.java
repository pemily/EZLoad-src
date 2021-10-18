package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface OperationData {

    String operation_date="opération.date";
    String operation_amount ="opération.montant";
    String operation_description="opération.description";
    String operation_accountType ="opération.typeCompte";
    String operation_quantity="opération.quantité";

    void fill(EzData data);
}
