package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.exporter.rules.RulesEngine;
import com.pascal.ezload.service.model.EZModel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EzReport {

    private List<EzEdition> ezEditions = new LinkedList<>();
    private List<String> errors = new LinkedList<>();
    private String sourceFile;

    public EzReport(){
    }

    public EzReport(EZModel fromEzModel){
        errors = fromEzModel.getErrors();
        sourceFile = fromEzModel.getSourceFile();
    }

    public List<EzEdition> getEzEditions() {
        return ezEditions;
    }

    public void setEzEditions(List<EzEdition> ezEditions) {
        this.ezEditions = ezEditions;
        if (ezEditions.stream().anyMatch(ez -> ez.getErrors().size() > 0)){
            if (ezEditions.stream().allMatch(ez -> ez.getErrors().stream().allMatch(e-> e.equals(RulesEngine.NO_RULE_FOUND)))){
                errors.add(RulesEngine.NO_RULE_FOUND);
            }
            else errors.add("Une errreur a été détectée dans une opération");
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setError(List<String> errors) {
        this.errors = errors;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }


}
