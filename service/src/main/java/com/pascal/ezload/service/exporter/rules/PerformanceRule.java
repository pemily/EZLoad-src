package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.StringValue;

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
