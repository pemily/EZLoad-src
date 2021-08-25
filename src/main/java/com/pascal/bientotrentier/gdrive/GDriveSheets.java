package com.pascal.bientotrentier.gdrive;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.util.BRException;
import com.pascal.bientotrentier.util.Sleep;
import com.pascal.bientotrentier.util.SupplierWithException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GDriveSheets {
    private final String spreadsheetId;
    private final Sheets service;
    private final Reporting reporting;

    public GDriveSheets(Reporting reporting, Sheets service, String spreadsheetId){
        this.spreadsheetId = spreadsheetId;
        this.service = service;
        this.reporting = reporting;
    }

    public SheetValues getCells(final String range) throws Exception {
        reporting.info("Google Drive reading data: "+range);
        List<List<Object>> r = retryOnTimeout(10, () -> {
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            return response.getValues();
        });
        reporting.info("Google Drive reading done");
        List<Row> rows = r.stream().map(Row::new).collect(Collectors.toList());
        return new SheetValues(range, rows);
    }

    public int update(String range, List<Row> values) throws Exception {
        reporting.info("Google Drive sheet updating: "+range);
        List<List<Object>> objValues = values.stream().map(Row::getValues).collect(Collectors.toList());
        int r = retryOnTimeout(10, () -> {
            UpdateValuesResponse resp = service.spreadsheets().values().update(spreadsheetId, range, new ValueRange().setValues(objValues))
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            return resp.getUpdatedCells();
        });
        reporting.info("Google Drive sheet updated");
        return r;
    }

    public List<SheetValues> batchGet(String... ranges) throws Exception {
        return batchGet(Arrays.asList(ranges));
    }

    public List<SheetValues> batchGet(List<String> ranges) throws Exception {
        return retryOnTimeout(10, () -> {
            BatchGetValuesResponse resp = service.spreadsheets()
                    .values()
                    .batchGet(spreadsheetId)
                    .setRanges(ranges)
                    .execute();
            return resp.getValueRanges().stream().map(vr ->
                new SheetValues(vr.getRange(),
                                vr.getValues().stream().map(Row::new).collect(Collectors.toList()))
            ).collect(Collectors.toList());
        });
    }

    public int batchUpdate(SheetValues... sheetValues) throws Exception {
        return batchUpdate(Arrays.asList(sheetValues));
    }

    public int batchUpdate(List<SheetValues> sheetValues) throws Exception {
        return retryOnTimeout(15, () -> {
            BatchUpdateValuesRequest buvr = new BatchUpdateValuesRequest();
            buvr.setValueInputOption("USER_ENTERED");

            buvr.setData(sheetValues.stream().map(sv -> {
                ValueRange vr = new ValueRange();
                vr.setValues(sv.getValues().stream().map(Row::getValues).collect(Collectors.toList()));
                vr.setRange(sv.getRange());
                return vr;
            }).collect(Collectors.toList()));

            BatchUpdateValuesResponse resp = service.spreadsheets().values().batchUpdate(spreadsheetId, buvr)
                    .execute();
            return resp.getTotalUpdatedCells();
        });
    }

    private <T> T retryOnTimeout(int n,  SupplierWithException<T> fct) throws Exception {
        try {
            return fct.get();
        }
        catch(java.net.SocketTimeoutException e){
            if ("Read timed out".equals(e.getMessage())){
                if (n == 0){
                    reporting.info("max retry reached");
                    throw new BRException("Google Drive API not accessible, please retry later");
                }
                reporting.info("Timeout reached, wait a little before retry");
                Sleep.wait(60);
                reporting.info("Retry n°: "+n);
                return retryOnTimeout(n - 1, fct);
            }
            throw e;
        }
    }
}
