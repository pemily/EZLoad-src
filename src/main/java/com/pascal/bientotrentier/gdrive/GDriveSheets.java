package com.pascal.bientotrentier.gdrive;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.util.BRException;
import com.pascal.bientotrentier.util.SupplierWithException;

import java.util.List;

public class GDriveSheets {
    private String spreadsheetId;
    private Sheets service;
    private Reporting reporting;

    public GDriveSheets(Reporting reporting, Sheets service, String spreadsheetId){
        this.spreadsheetId = spreadsheetId;
        this.service = service;
        this.reporting = reporting;
    }

    public List<List<Object>> getCells(final String range) throws Exception {
        reporting.info("GDrive reading data: "+range);
        List<List<Object>> r = retryOnTimeout(10, () -> {
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            return response.getValues();
        });
        reporting.info("GDrive reading done");
        return r;
    }

    public int update(String range, List<List<Object>> values) throws Exception {
        reporting.info("GDrive sheet updating: "+range);
        int r = retryOnTimeout(10, () -> {
            UpdateValuesResponse resp = service.spreadsheets().values().update(spreadsheetId, range, new ValueRange().setValues(values))
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            return resp.getUpdatedCells();
        });
        reporting.info("GDrive sheet updated");
        return r;
    }


    private <T> T retryOnTimeout(int n,  SupplierWithException<T> fct) throws Exception {
        try {
            return fct.get();
        }
        catch(java.net.SocketTimeoutException e){
            if ("Read timed out".equals(e.getMessage())){
                try {
                    Thread.sleep(10 * 1000);
                }
                catch (InterruptedException ie){}
                if (n == 0){
                    reporting.info("max retry reached");
                    throw new BRException("Google Drive not accessible, please retry later");
                }
                else {
                    reporting.info("request timeout => retry n°: "+n);
                    retryOnTimeout(n - 1, fct);
                }
            }
            throw e;
        }
    }
}
