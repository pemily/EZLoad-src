package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.util.StringValue;

import java.util.LinkedList;
import java.util.List;

public class RuleDefinition extends RuleDefinitionSummary {

    public enum Field{name, description, condition }

    private int ezLoadVersion; // the version of EzLoad when this RuleDefinition was created/edited
    private String condition;
    private OperationRule operationRule; // can be null si le repport pdf n'a aucun impact sur le portefeuille ou les opérations.
    private List<PortefeuilleRule> portefeuilleRules = new LinkedList<>();

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }


    public int getEzLoadVersion() {
        return ezLoadVersion;
    }

    public void setEzLoadVersion(int ezLoadVersion) {
        this.ezLoadVersion = ezLoadVersion;
    }

    public List<PortefeuilleRule> getPortefeuilleRules() {
        return portefeuilleRules;
    }

    public void setPortefeuilleRules(List<PortefeuilleRule> portefeuilleRules) {
        this.portefeuilleRules = portefeuilleRules;
    }

    public OperationRule getOperationRule() {
        return operationRule;
    }

    public void setOperationRule(OperationRule operationRule) {
        this.operationRule = operationRule;
    }

    @Override
    public void clearErrors(){
        setField2ErrorMsg(null);
        portefeuilleRules.forEach(PortefeuilleRule::clearErrors);
        if (operationRule != null) operationRule.clearErrors();
    }


    public void validate(){
        // TODO ici vérifiez que chaque expression n'utilise pas une variable qui n'existe pas et que l'expression est correcte
        // faire une dummy operation est l'executer sur chaque expression?

        new StringValue(true).validate(this, Field.name.name(), getName());
        new StringValue(false).validate(this, Field.description.name(), getDescription());
        new StringValue(true).validate(this, Field.condition.name(), condition);

        if (operationRule != null) operationRule.validate();
        portefeuilleRules.forEach(PortefeuilleRule::validate);
    }
}
