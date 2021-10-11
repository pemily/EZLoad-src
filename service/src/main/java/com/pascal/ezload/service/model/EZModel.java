package com.pascal.ezload.service.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EZModel {

    private EnumEZCourtier courtier;

    private String sourceFile;

    private EZDate reportDate;

    private EZAccount account;

    private EZAccountDeclaration ezAccountDeclaration;

    private List<EZOperation> operations = new LinkedList<>();

    private boolean error;

    public EZModel(EnumEZCourtier courtier, String sourceFile){
        this.courtier = courtier;
        this.sourceFile = sourceFile;
    }

    public boolean getError(){
        return error;
    }

    public void setError(boolean error){
        this.error = error;
    }

    public EnumEZCourtier getCourtier() {
        return courtier;
    }

    public void setCourtier(EnumEZCourtier courtier) {
        this.courtier = courtier;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public EZDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(EZDate reportDate) {
        this.reportDate = reportDate;
    }

    public EZAccount getAccount() {
        return account;
    }

    public void setAccount(EZAccount account) {
        this.account = account;
    }

    public List<EZOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<EZOperation> operations) {
        this.operations = operations;
    }

    public EZAccountDeclaration getAccountDeclaration() {
        return ezAccountDeclaration;
    }

    public void setAccountDeclaration(EZAccountDeclaration ezAccountDeclaration) {
        this.ezAccountDeclaration = ezAccountDeclaration;
    }

    public void fill(Map<String, String> data) {
        data.put("rapport.courtier", courtier.getEzPortfolioName());
        data.put("rapport.source", sourceFile);
        data.put("rapport.date", reportDate == null ? null : reportDate.toEzPortoflioDate());
        data.put("rapport.numeroCompte", account == null ?  null : account.getAccountNumber());
        data.put("rapport.typeCompte", account == null ?  null : account.getAccountType());
        data.put("rapport.nomCompte", ezAccountDeclaration == null ? null : ezAccountDeclaration.getName());
        data.put("rapport.symbolDevise", account == null ?  null : account.getDevise().getSymbol());
        data.put("rapport.codeDevise", account == null ?  null : account.getDevise().getCode());
        data.put("rapport.proprietaire", account == null ?  null : account.getOwnerName());
        data.put("rapport.addresse", account == null ?  null : account.getOwnerAdress());
    }
}

