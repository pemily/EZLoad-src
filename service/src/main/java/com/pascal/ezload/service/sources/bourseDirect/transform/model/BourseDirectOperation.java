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
package com.pascal.ezload.service.sources.bourseDirect.transform.model;

import com.pascal.ezload.service.model.EZDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BourseDirectOperation {
    private int pdfPage;
    private float pdfPositionDateY;
    private EZDate date;
    private ArrayList<String> operationDescription = new ArrayList<>();
    private Map<String, String> fields = new HashMap<>();

    public EZDate getDate() {
        return date;
    }

    public void setDate(EZDate date) {
        this.date = date;
    }

    public ArrayList<String> getOperationDescription() {
        return operationDescription;
    }

    public void setOperationDescription(ArrayList<String> operationDescription) {
        this.operationDescription = operationDescription;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public float getPdfPositionDateY() {
        return pdfPositionDateY;
    }

    public void setPdfPositionDateY(float pdfPositionDateY) {
        this.pdfPositionDateY = pdfPositionDateY;
    }

    public int getPdfPage() {
        return pdfPage;
    }

    public void setPdfPage(int pdfPage) {
        this.pdfPage = pdfPage;
    }
}
