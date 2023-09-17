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
package com.pascal.ezload.service.gdrive;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.BRException;
import com.pascal.ezload.service.util.Sleep;
import com.pascal.ezload.service.util.StringUtils;
import com.pascal.ezload.service.util.SupplierThatThrow;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GDriveSheets {
    private static final int RETRY_NB = 20;

    private final String ezPortfolioUrl;
    private final String spreadsheetId;
    private final Sheets service;
    private final Map<String, Integer> sheetName2sheetId = new HashMap<>();

    public GDriveSheets(Sheets service, String ezPortfolioUrl){
        this.ezPortfolioUrl = ezPortfolioUrl;
        this.service = service;
        try {
            String next = ezPortfolioUrl.substring(SettingsManager.EZPORTFOLIO_GDRIVE_URL_PREFIX.length());
            this.spreadsheetId = StringUtils.divide(next, '/')[0];
        }
        catch(Exception e){
            String errorMsg = "Impossible d'extraire l'identifiant de document de ezPortfolio "+ezPortfolioUrl;
            throw new IllegalArgumentException(errorMsg, e);
        }
    }

    public String getEzPortfolioUrl(){
        return ezPortfolioUrl;
    }

    public SheetValues getCells(Reporting reporting, final String range) throws Exception {
        reporting.info("Google Drive lecture des données: "+range);
        List<List<Object>> r = retryOnTimeout(reporting, RETRY_NB, () -> {
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            return response.getValues();
        });
        reporting.info("Google Drive lecture OK");
        return SheetValues.createFromObjectLists(range, r);
    }

    public void createSheet(String tabTitle) throws IOException {
        // https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets/request#addsheetrequest
        // https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets/request#AddChartRequest
        /// return sheetsService.spreadsheets().batchUpdate(spreadSheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests)).execute();
        Request request = new Request();
        request.setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle(tabTitle).setHidden(true)));
        try {
            BatchUpdateSpreadsheetResponse resp = service.spreadsheets()
                    .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(List.of(request))).execute();
        }
        catch (GoogleJsonResponseException ex){
            boolean alreadyExists = ex.getMessage() != null
                    && (ex.getMessage().contains("déjà") || ex.getMessage().contains("already"));
            if  (!alreadyExists){
                throw ex;
            }
        }
    }

    public int update(Reporting reporting, String range, List<Row> values) throws Exception {
        reporting.info("Mise à jour de Google Drive: "+range);
        List<List<Object>> objValues = values.stream().map(Row::getValues).collect(Collectors.toList());
        int r = retryOnTimeout(reporting, RETRY_NB, () -> {
            UpdateValuesResponse resp = service.spreadsheets().values().update(spreadsheetId, range, new ValueRange().setValues(objValues))
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            return resp.getUpdatedCells();
        });
        reporting.info("Mise à jour Google Drive OK");
        return r;
    }

    public List<SheetValues> batchGet(Reporting reporting, String... ranges) throws Exception {
        return batchGet(reporting, Arrays.asList(ranges));
    }

    public List<SheetValues> batchGet(Reporting reporting, List<String> ranges) throws Exception {
        return retryOnTimeout(reporting, RETRY_NB, () -> {
            BatchGetValuesResponse resp = service.spreadsheets()
                    .values()
                    .batchGet(spreadsheetId)
                    .setValueRenderOption("FORMULA") // https://developers.google.com/sheets/api/reference/rest/v4/ValueRenderOption
                    .setRanges(ranges)
                    .execute();
            return resp.getValueRanges().stream().map(vr -> SheetValues.createFromObjectLists(vr.getRange(), vr.getValues())).collect(Collectors.toList());
        });
    }

    public int batchUpdate(Reporting reporting, SheetValues... sheetValues) throws Exception {
        return batchUpdate(reporting, Arrays.asList(sheetValues));
    }

    public int batchUpdate(Reporting reporting, List<SheetValues> sheetValues) throws Exception {
        reporting.info("Mise à jour de Google Drive: "+sheetValues.stream().map(SheetValues::getRange).collect(Collectors.joining(" & ")));
        return retryOnTimeout(reporting, RETRY_NB, () -> {
            BatchUpdateValuesRequest buvr = new BatchUpdateValuesRequest();
            buvr.setValueInputOption("USER_ENTERED"); // https://developers.google.com/sheets/api/reference/rest/v4/ValueInputOption

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

    public void applyRequest(List<Request> requests) throws IOException {
        BatchUpdateSpreadsheetRequest rq = new BatchUpdateSpreadsheetRequest();
        rq.setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, rq).execute();
    }

    private <T> T retryOnTimeout(Reporting reporting, int n,  SupplierThatThrow<T> fct) throws Exception {
        try {
            return fct.get();
        }
        catch(java.net.SocketTimeoutException e){
            if ("Read timed out".equals(e.getMessage())){
                if (n == 0){
                    reporting.info("Relance maximum atteinte");
                    throw new BRException("Google Drive API n'est pas accessible, re-essayez plus tard");
                }
                reporting.info("Delai dépassé. attendre 30 secondes... puis relance");
                Sleep.waitSeconds(30);
                reporting.info("Relance n°: "+n);
                return retryOnTimeout(reporting, n - 1, fct);
            }
            throw e;
        }
    }

    public void init(Reporting reporting) throws Exception {
        retryOnTimeout(reporting, RETRY_NB, () -> {
            Spreadsheet o = service.spreadsheets().get(spreadsheetId).execute();
            o.getSheets().forEach(s ->  sheetName2sheetId.put(s.getProperties().getTitle(), s.getProperties().getSheetId()));
            return null;
        });
    }

    public Integer getSheetId(String sheetName){
        return sheetName2sheetId.get(sheetName);
    }
}
