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
