package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.model.EZOperation;

import java.util.LinkedList;
import java.util.List;

// une EzEdition represente le resultat d'une régle
// c'est a dire plusieurs operations sur les les feuilles MesOperations & Portefeuille
// une EzEdition est le resultat d'une operation du relevé de banque
public class EzEdition {

    private String id;
    private RuleDefinitionSummary ruleDefinitionSummary;
    private List<EzOperationEdition> ezOperationEditions = new LinkedList<>();
    private List<EzPortefeuilleEdition> ezPortefeuilleEditions = new LinkedList<>();
    private List<String> errors = new LinkedList<>();

    private EzData data = new EzData();

    public EzEdition(){
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public List<EzOperationEdition> getEzOperationEditions() {
        return ezOperationEditions;
    }

    public void setEzOperationEditions(List<EzOperationEdition> ezOperationEdition) {
        this.ezOperationEditions = ezOperationEdition;
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
