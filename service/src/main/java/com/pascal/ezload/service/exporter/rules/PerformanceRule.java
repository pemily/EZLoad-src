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

import com.pascal.ezload.common.util.Checkable;
import com.pascal.ezload.common.util.StringValue;

import java.util.function.Function;

public class PerformanceRule extends Checkable<PerformanceRule> {

    public enum Field{ condition, inputOutputValueExpr}

    private String condition;
    private String inputOutputValueExpr;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getInputOutputValueExpr() {
        return inputOutputValueExpr;
    }

    public void setInputOutputValueExpr(String inputOutputValueExpr) {
        this.inputOutputValueExpr = inputOutputValueExpr;
    }

    @Override
    public PerformanceRule validate() {
        new StringValue(this, PerformanceRule.Field.condition.name(), condition); // no check
        new StringValue(this, PerformanceRule.Field.inputOutputValueExpr.name(), inputOutputValueExpr);  // no check
        return this;
    }

    public void beforeSave(Function<String, String> normalizer) {
        this.condition = normalizer.apply(this.condition);
        this.inputOutputValueExpr = normalizer.apply(this.inputOutputValueExpr);
    }
}
