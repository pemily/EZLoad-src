package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.util.StringValue;

import java.util.LinkedList;
import java.util.List;

public class RuleDefinition extends RuleDefinitionSummary {

    public enum Field{name, description, condition }

    private int ezLoadVersion; // the version of EzLoad when this RuleDefinition was created/edited
    private String condition;
    private List<OperationRule> operationRules = new LinkedList<>();
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

    public List<OperationRule> getOperationRules() {
        return operationRules;
    }

    public void setOperationRules(List<OperationRule> operationRules) {
        this.operationRules = operationRules;
    }

    @Override
    public void clearErrors(){
        setField2ErrorMsg(null);
        portefeuilleRules.forEach(PortefeuilleRule::clearErrors);
        operationRules.forEach(OperationRule::clearErrors);
    }


    public void validate(){
        // TODO ici vérifiez que chaque expression n'utilise pas une variable qui n'existe pas et que l'expression est correcte

        new StringValue(true).validate(this, Field.name.name(), getName());
        new StringValue(false).validate(this, Field.description.name(), getDescription());
        new StringValue(true).validate(this, Field.condition.name(), condition);

        operationRules.forEach(OperationRule::validate);
        portefeuilleRules.forEach(PortefeuilleRule::validate);
    }
}
