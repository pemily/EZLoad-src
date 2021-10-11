package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.model.EZModel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EzReport {

    private List<EzEdition> ezEditions = new LinkedList<>();
    private String error;
    private String sourceFile;
    private Map<String, String> data = new HashMap<>();

    public EzReport(){
    }

    public EzReport(EZModel fromEzModel){
        error = fromEzModel.getError() ? "Une erreur a été détectée dans le rapport" : null;
        sourceFile = fromEzModel.getSourceFile();
        fromEzModel.fill(data);
    }

    public List<EzEdition> getEzEditions() {
        return ezEditions;
    }

    public void setEzEditions(List<EzEdition> ezEditions) {
        this.ezEditions = ezEditions;
        if (error == null)
            error = ezEditions.stream().anyMatch(ez -> ez.getError() != null) ? "Une errreur a été détectée dans une opération" : null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
