package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.model.EZOperation;

import java.util.LinkedList;
import java.util.List;

// une operation qui impact la feuille MesOperations & Portefeuille
public class EzEdition {

    private RuleDefinitionSummary ruleDefinitionSummary;
    private EzOperationEdition ezOperationEdition; // can be null if no impact
    private List<EzPortefeuilleEdition> ezPortefeuilleEditions = new LinkedList<>();
    private List<String> errors = new LinkedList<>();

    private EzData data = new EzData();

    public EzEdition(){}

    public EzEdition(EZOperation fromEzOperation, EzOperationEdition ezOperationEdition, List<EzPortefeuilleEdition> ezPortefeuilleEditions) {
        fromEzOperation.fill(data);
        this.ezOperationEdition = ezOperationEdition;
        this.ezPortefeuilleEditions = ezPortefeuilleEditions;
        this.errors = fromEzOperation.getErrors();
    }


    public EzOperationEdition getEzOperationEdition() {
        return ezOperationEdition;
    }

    public void setEzOperationEdition(EzOperationEdition ezOperationEdition) {
        this.ezOperationEdition = ezOperationEdition;
    }

    public List<EzPortefeuilleEdition> getEzPortefeuilleEditions() {
        return ezPortefeuilleEditions;
    }

    public void setEzPortefeuilleEditions(List<EzPortefeuilleEdition> ezPortefeuilleEditions) {
        this.ezPortefeuilleEditions = ezPortefeuilleEditions;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public EzData getData() {
        return data;
    }

    public void setData(EzData data) {
        this.data = data;
    }

    public RuleDefinitionSummary getRuleDefinitionSummary() {
        return ruleDefinitionSummary;
    }

    public void setRuleDefinitionSummary(RuleDefinitionSummary ruleDefinitionSummary) {
        this.ruleDefinitionSummary = ruleDefinitionSummary;
    }
}
