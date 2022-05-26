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
package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.exporter.rules.RulesEngine;
import com.pascal.ezload.service.model.EZModel;

import java.util.LinkedList;
import java.util.List;

public class EzReport {

    public enum EnumStatus { OK, WARNING, ERROR }
    public enum ReportType { IS_DIVIDEND_UPDATE, IS_SHARE_UPDATE }

    private List<EzEdition> ezEditions = new LinkedList<>();
    private List<String> errors = new LinkedList<>();
    private String sourceFile;
    private EnumStatus status;
    private ReportType reportType;

    public EzReport(){
        reportType = ReportType.IS_SHARE_UPDATE;
    }

    public EzReport(EZModel fromEzModel){
        errors = fromEzModel.getErrors();
        status = errors.size() > 0 ? EnumStatus.ERROR : EnumStatus.OK;
        sourceFile = fromEzModel.getSourceFile();
        reportType = ReportType.IS_SHARE_UPDATE;
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

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

}
