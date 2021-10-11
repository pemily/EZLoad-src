package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.model.EZOperation;

import java.util.HashMap;
import java.util.Map;

// une operation qui impact la feuille MesOperations & Portefeuille
public class EzEdition {

    private EzOperationEdition ezOperationEdition;
    private EzPortefeuilleEdition ezPortefeuilleEdition;
    private String error;

    private Map<String, String> data = new HashMap<>();

    public EzEdition(){}

    public EzEdition(EZOperation fromEzOperation, EzOperationEdition ezOperationEdition, EzPortefeuilleEdition ezPortefeuilleEdition) {
        fromEzOperation.fill(data);
        this.ezOperationEdition = ezOperationEdition;
        this.ezPortefeuilleEdition = ezPortefeuilleEdition;
        this.error = fromEzOperation.getError() ? "Une erreur à été détectée dans l'opération" : null;
    }


    public EzOperationEdition getEzOperationEdition() {
        return ezOperationEdition;
    }

    public void setEzOperationEdition(EzOperationEdition ezOperationEdition) {
        this.ezOperationEdition = ezOperationEdition;
    }

    public EzPortefeuilleEdition getEzPortefeuilleEdition() {
        return ezPortefeuilleEdition;
    }

    public void setEzPortefeuilleEdition(EzPortefeuilleEdition ezPortefeuilleEdition) {
        this.ezPortefeuilleEdition = ezPortefeuilleEdition;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
