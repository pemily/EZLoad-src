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

import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.StringValue;

import java.util.function.Function;

public class OperationRule extends Checkable<OperationRule> {

    public enum Field{ condition, operationDateExpr, operationCompteTypeExpr, operationBrokerExpr,
        operationQuantityExpr, operationTypeExpr, operationActionNameExpr, operationCountryExpr, operationAmountExpr,
        operationDescriptionExpr}

    private String condition;
    private String operationDateExpr;
    private String operationCompteTypeExpr;
    private String operationBrokerExpr;
    private String operationQuantityExpr;
    private String operationTypeExpr;
    private String operationActionNameExpr;
    private String operationCountryExpr;
    private String operationAmountExpr;
    private String operationDescriptionExpr;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getOperationDateExpr() {
        return operationDateExpr;
    }

    public void setOperationDateExpr(String operationDateExpr) {
        this.operationDateExpr = operationDateExpr;
    }

    public String getOperationCompteTypeExpr() {
        return operationCompteTypeExpr;
    }

    public void setOperationCompteTypeExpr(String operationCompteTypeExpr) {
        this.operationCompteTypeExpr = operationCompteTypeExpr;
    }

    public String getOperationBrokerExpr() {
        return operationBrokerExpr;
    }

    public void setOperationBrokerExpr(String operationBrokerExpr) {
        this.operationBrokerExpr = operationBrokerExpr;
    }

    public String getOperationQuantityExpr() {
        return operationQuantityExpr;
    }

    public void setOperationQuantityExpr(String operationQuantityExpr) {
        this.operationQuantityExpr = operationQuantityExpr;
    }

    public String getOperationTypeExpr() {
        return operationTypeExpr;
    }

    public void setOperationTypeExpr(String operationTypeExpr) {
        this.operationTypeExpr = operationTypeExpr;
    }

    public String getOperationActionNameExpr() {
        return operationActionNameExpr;
    }

    public void setOperationActionNameExpr(String operationActionNameExpr) {
        this.operationActionNameExpr = operationActionNameExpr;
    }

    public String getOperationCountryExpr() {
        return operationCountryExpr;
    }

    public void setOperationCountryExpr(String operationCountryExpr) {
        this.operationCountryExpr = operationCountryExpr;
    }

    public String getOperationAmountExpr() {
        return operationAmountExpr;
    }

    public void setOperationAmountExpr(String operationAmountExpr) {
        this.operationAmountExpr = operationAmountExpr;
    }

    public String getOperationDescriptionExpr() {
        return operationDescriptionExpr;
    }

    public void setOperationDescriptionExpr(String operationDescriptionExpr) {
        this.operationDescriptionExpr = operationDescriptionExpr;
    }

    @Override
    public OperationRule validate() {
        new StringValue(this, Field.condition.name(), condition); // no check
        new StringValue(this, Field.operationDateExpr.name(), operationDateExpr).checkRequired();
        new StringValue(this, Field.operationCompteTypeExpr.name(), operationCompteTypeExpr).checkRequired();
        new StringValue(this, Field.operationBrokerExpr.name(), operationBrokerExpr).checkRequired();
        new StringValue(this, Field.operationQuantityExpr.name(), operationQuantityExpr).checkRequired();
        new StringValue(this, Field.operationTypeExpr.name(), operationTypeExpr).checkRequired()
                .validateWithLimitedValues(
                        "\"Achat titres\"",
                        "\"Acompte Impôt sur le Revenu\"",
                        "\"Courtage sur achat de titres\"",
                        "\"Courtage sur vente de titres\"",
                        "\"Divers\"",
                        "\"Dividende brut\"",
                        "\"Dividende brut NON soumis à abattement\"",
                        "\"Dividende brut soumis à abattement\"",
                        "\"Dividende versé\"",
                        "\"Droits de garde/Frais divers\"",
                        "\"Prélèvements sociaux\"",
                        "\"Prélèvements sociaux sur retrait PEA\"",
                        "\"Retenue fiscale\"",
                        "\"Retrait fonds\"",
                        "\"Taxe sur les Transactions\"",
                        "\"Vente titres\"",
                        "\"Versement fonds\""
                );
        new StringValue(this, Field.operationActionNameExpr.name(), operationActionNameExpr).checkRequired();
        new StringValue(this, Field.operationCountryExpr.name(), operationCountryExpr).checkRequired();
        new StringValue(this, Field.operationAmountExpr.name(), operationAmountExpr).checkRequired();
        new StringValue(this, Field.operationDescriptionExpr.name(), operationDescriptionExpr).checkRequired();
        return this;
    }

    public void beforeSave(Function<String, String> normalizer){
      this.condition = normalizer.apply(this.condition);
      this.operationDateExpr = normalizer.apply(this.operationDateExpr);
      this.operationCompteTypeExpr = normalizer.apply(this.operationCompteTypeExpr);
      this.operationBrokerExpr = normalizer.apply(this.operationBrokerExpr);
      this.operationQuantityExpr = normalizer.apply(this.operationQuantityExpr);
      this.operationTypeExpr = normalizer.apply(this.operationTypeExpr);
      this.operationActionNameExpr = normalizer.apply(this.operationActionNameExpr);
      this.operationCountryExpr = normalizer.apply(this.operationCountryExpr);
      this.operationAmountExpr = normalizer.apply(this.operationAmountExpr);
      this.operationDescriptionExpr = normalizer.apply(this.operationDescriptionExpr);
    }
}
