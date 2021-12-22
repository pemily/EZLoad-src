package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.exporter.rules.RulesEngine;
import com.pascal.ezload.service.model.EZModel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EzReport {

    public enum EnumStatus { OK, WARNING, ERROR }
    private List<EzEdition> ezEditions = new LinkedList<>();
    private List<String> errors = new LinkedList<>();
    private String sourceFile;
    private EnumStatus status;

    public EzReport(){
    }

    public EzReport(EZModel fromEzModel){
        errors = fromEzModel.getErrors();
        status = errors.size() > 0 ? EnumStatus.ERROR : EnumStatus.OK;
        sourceFile = fromEzModel.getSourceFile();
    }

    public List<EzEdition> getEzEditions() {
        return ezEditions;
    }

    public void setEzEditions(List<EzEdition> ezEditions) {
        this.ezEditions = ezEditions;
        if (ezEditions.stream().anyMatch(ez -> ez.getErrors().size() > 0)){
            if (ezEditions.stream().allMatch(ez -> ez.getErrors().stream().allMatch(e-> e.equals(RulesEngine.NO_RULE_FOUND)))){
                status = status == EnumStatus.ERROR ? status : EnumStatus.WARNING;
            }
            else status = EnumStatus.ERROR;
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public EnumStatus getStatus() {
        return status;
    }

    public void setStatus(EnumStatus status) {
        this.status = status;
    }
}
