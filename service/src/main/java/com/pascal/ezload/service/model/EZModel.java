package com.pascal.ezload.service.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EZModel {

    private EnumEZBroker broker;
    private int brokerFileVersion;

    private String sourceFile;

    private EZDate reportDate;

    private EZAccount account;

    private EZAccountDeclaration ezAccountDeclaration;

    private List<EZOperation> operations = new LinkedList<>();

    private List<String> errors = new LinkedList<>();

    public EZModel(EnumEZBroker broker, int brokerFileVersion, String sourceFile){
        this.broker = broker;
        this.sourceFile = sourceFile;
        this.brokerFileVersion = brokerFileVersion;
    }

    public List<String> getErrors(){
        return errors;
    }

    public void setErrors(List<String> errors){
        this.errors = errors;
    }

    public EnumEZBroker getBroker() {
        return broker;
    }

    public void setBroker(EnumEZBroker broker) {
        this.broker = broker;
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
        data.put("rapport.courtier", broker.getEzPortfolioName());
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

