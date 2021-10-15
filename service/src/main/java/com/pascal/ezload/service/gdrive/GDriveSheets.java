package com.pascal.ezload.service.gdrive;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.BRException;
import com.pascal.ezload.service.util.Sleep;
import com.pascal.ezload.service.util.StringUtils;
import com.pascal.ezload.service.util.SupplierWithException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GDriveSheets {
    private final String ezPortfolioUrl;
    private final String spreadsheetId;
    private final Sheets service;
    private final Reporting reporting;

    public GDriveSheets(Reporting reporting, Sheets service, String ezPortfolioUrl){
        this.ezPortfolioUrl = ezPortfolioUrl;
        try {
            String next = ezPortfolioUrl.substring(SettingsManager.EZPORTFOLIO_GDRIVE_URL_PREFIX.length());
            this.spreadsheetId = StringUtils.divide(next, '/')[0];
        }
        catch(Exception e){
            String errorMsg = "Impossible d'extraire l'identifiant de document de ezPortfolio "+ezPortfolioUrl;
            reporting.error(errorMsg);
            throw new IllegalArgumentException(errorMsg, e);
        }
        this.service = service;
        this.reporting = reporting;
    }

    public String getEzPortfolioUrl(){
        return ezPortfolioUrl;
    }

    public SheetValues getCells(final String range) throws Exception {
        reporting.info("Google Drive lecture des données: "+range);
        List<List<Object>> r = retryOnTimeout(10, () -> {
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            return response.getValues();
        });
        reporting.info("Google Drive lecture OK");
        List<Row> rows = r.stream().map(Row::new).collect(Collectors.toList());
        return new SheetValues(range, rows);
    }

    public int update(String range, List<Row> values) throws Exception {
        reporting.info("Mise à jour de Google Drive: "+range);
        List<List<Object>> objValues = values.stream().map(Row::getValues).collect(Collectors.toList());
        int r = retryOnTimeout(10, () -> {
            UpdateValuesResponse resp = service.spreadsheets().values().update(spreadsheetId, range, new ValueRange().setValues(objValues))
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            return resp.getUpdatedCells();
        });
        reporting.info("Mise à jour Google Drive OK");
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
            return resp.getValueRanges().stream().map(vr -> {
                List<Row> rows = vr.getValues() == null ? new LinkedList<>() : vr.getValues().stream().map(Row::new).collect(Collectors.toList());
                return new SheetValues(vr.getRange(), rows);
            }
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
                    reporting.info("Relance maximum atteinte");
                    throw new BRException("Google Drive API n'est pas accessible, re-essayez plus tard");
                }
                reporting.info("Delai dépassé. attendre un peu... puis relance");
                Sleep.waitSeconds(60);
                reporting.info("Relance n°: "+n);
                return retryOnTimeout(n - 1, fct);
            }
            throw e;
        }
    }
}
