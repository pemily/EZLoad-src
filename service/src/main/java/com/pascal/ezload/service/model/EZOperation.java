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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;
import com.pascal.ezload.service.exporter.ezEdition.data.common.OperationData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class EZOperation implements OperationData {

    private List<String> errors = new LinkedList<>();
    private EZDate date;
    private ArrayList<String> designation;
    private Map<String, String> fields;

    @JsonIgnore
    private EnumEZBroker broker;
    @JsonIgnore
    private EZAccount account;
    @JsonIgnore
    private EZAccountDeclaration ezAccountDeclaration;

    public EZDate getDate() {
        return date;
    }

    public void setDate(EZDate date) {
        this.date = date;
    }

    public EnumEZBroker getBroker() {
        return broker;
    }

    public void setBroker(EnumEZBroker broker) {
        this.broker = broker;
    }

    public EZAccount getAccount() {
        return account;
    }

    public void setAccount(EZAccount account) {
        this.account = account;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public EZAccountDeclaration getEzAccountDeclaration() {
        return ezAccountDeclaration;
    }

    public void setEzAccountDeclaration(EZAccountDeclaration ezAccountDeclaration) {
        this.ezAccountDeclaration = ezAccountDeclaration;
    }

    @Override
    public String toString() {
        return "EZOperation{" +
                "date='" + date + '\'' +
                ", account Name=" + account.getOwnerName() +
                ", account Number=" + account.getAccountNumber() +
                ", courtier='" + broker + '\'' +
                '}';
    }


    public void fill(EzData data) {
        data.put(operation_date, date.toEzPortoflioDate());

        for (int i = 0; i < designation.size(); i++){
            data.put(new EzDataKey(OperationData.EZOperationDesignation+(i+1), "Champ trouvé dans la désignation de l'opération"), designation.get(i));
        }

        this.fields.forEach((key, value) -> data.put(new EzDataKey(key, ""), value));

        broker.fill(data);
        account.fill(data);
        ezAccountDeclaration.fill(data);
    }

    public ArrayList<String> getDesignation() {
        return designation;
    }

    public void setDesignation(ArrayList<String> designation) {
        this.designation = designation;
    }


    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public Map<String, String> getFields(){
        return fields; // this method is used to display the json format
    }
}
