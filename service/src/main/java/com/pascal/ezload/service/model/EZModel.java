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
package com.pascal.ezload.service.model;

import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.ReportData;

import java.util.LinkedList;
import java.util.List;

public class EZModel implements ReportData {

    private EnumEZBroker broker;
    private int brokerFileVersion;

    private String sourceFile;

    private EZDate reportDate;

    private EZAccount account;

    private EZAccountDeclaration ezAccountDeclaration;

    private List<EZOperation> operations = new LinkedList<>();

    private List<String> errors = new LinkedList<>();

    public EZModel(){
        // for json deserializer
    }

    public EZModel(EnumEZBroker broker, int brokerFileVersion, String sourceFile){
        this.broker = broker;
        this.sourceFile = sourceFile;
        this.brokerFileVersion = brokerFileVersion;
    }

    public int getBrokerFileVersion() {
        return brokerFileVersion;
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
        // data.put(report_date, reportDate == null ? null : reportDate.toEzPortoflioDate());
    }
}

