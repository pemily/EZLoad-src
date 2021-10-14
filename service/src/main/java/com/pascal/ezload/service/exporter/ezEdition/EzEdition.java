package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.model.EZOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// une operation qui impact la feuille MesOperations & Portefeuille
public class EzEdition {

    private EzOperationEdition ezOperationEdition;
    private EzPortefeuilleEdition ezPortefeuilleEdition;
    private List<String> errors;

    private EzData data = new EzData();

    public EzEdition(){}

    public EzEdition(EZOperation fromEzOperation, EzOperationEdition ezOperationEdition, EzPortefeuilleEdition ezPortefeuilleEdition) {
        fromEzOperation.fill(data);
        this.ezOperationEdition = ezOperationEdition;
        this.ezPortefeuilleEdition = ezPortefeuilleEdition;
        this.errors = fromEzOperation.getErrors();
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

    public List<String> getErrors() {
        return errors;
    }

    public void setError(List<String> errors) {
        this.errors = errors;
    }

    public EzData getData() {
        return data;
    }

    public void setData(EzData data) {
        this.data = data;
    }
}
