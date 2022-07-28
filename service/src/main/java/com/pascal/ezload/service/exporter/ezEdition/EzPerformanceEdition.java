package com.pascal.ezload.service.exporter.ezEdition;

public class EzPerformanceEdition implements WithErrors {

    private String errors;

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getErrors() {
        return errors;
    }

    @Override
    public void setErrors(String errors) {
        this.errors = errors;
    }

}
