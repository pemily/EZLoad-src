package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.BourseDirectV1Data;
import com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.ReportData;

import java.util.LinkedList;
import java.util.List;

public class EZModel implements BourseDirectV1Data, ReportData {

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

    public void fill(EzData data) {
        broker.fill(data);
        account.fill(data);
        ezAccountDeclaration.fill(data);

        data.put(BrokerData.broker_version, brokerFileVersion+"");
        data.put(report_source, sourceFile);
        data.put(report_date, reportDate == null ? null : reportDate.toEzPortoflioDate());
    }
}

