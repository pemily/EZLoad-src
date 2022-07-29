/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.util.StringValue;
import com.pascal.ezload.service.util.StringValues;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RuleDefinition extends RuleDefinitionSummary {

    public enum Field{name, description, condition, shareId }

    private int ezLoadVersion; // the version of EzLoad when this RuleDefinition was created/edited
    private String condition;
    private String shareId; // the share id example: US5024311095 / FR0013269123
    private List<OperationRule> operationRules = new LinkedList<>();
    private List<PortefeuilleRule> portefeuilleRules = new LinkedList<>();
    private PerformanceRule performanceRule = new PerformanceRule();

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

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public PerformanceRule getPerformanceRule(){
        return performanceRule;
    }

    public void setPerformanceRule(PerformanceRule performanceRule){
        this.performanceRule = performanceRule;
    }

    @Override
    public void clearErrors(){
        setField2ErrorMsg(null);
        portefeuilleRules.forEach(PortefeuilleRule::clearErrors);
        operationRules.forEach(OperationRule::clearErrors);
        performanceRule.clearErrors();
    }


    public RuleDefinition validate(){
        // TODO ici vérifiez que chaque expression n'utilise pas une variable qui n'existe pas et que l'expression est correcte

        new StringValue(this, Field.name.name(), getName()).checkRequired();
        new StringValues(this, Field.description.name(), getDescription()); // no check
        new StringValue(this, Field.condition.name(), condition).checkRequired();
        new StringValue(this, Field.shareId.name(), shareId); // no check

        operationRules.forEach(OperationRule::validate);
        portefeuilleRules.forEach(PortefeuilleRule::validate);
        performanceRule.validate();
        return this;
    }

    public void beforeSave(Function<String, String> normalizer){
        super.beforeSave(normalizer);
        this.condition = normalizer.apply(this.condition);
        this.shareId = normalizer.apply(this.shareId);
        this.operationRules.forEach(op -> op.beforeSave(normalizer));
        this.portefeuilleRules.forEach(po -> po.beforeSave(normalizer));
        this.performanceRule.beforeSave(normalizer);
    }
}

