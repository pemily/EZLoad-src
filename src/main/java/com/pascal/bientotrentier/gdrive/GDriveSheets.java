package com.pascal.bientotrentier.gdrive;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.List;

public class GDriveSheets {
    private String spreadsheetId;
    private Sheets service;

    public GDriveSheets(Sheets service, String spreadsheetId){
        this.spreadsheetId = spreadsheetId;
        this.service = service;
    }

    public List<List<Object>> getCells(final String range) throws IOException {
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    public int update(String range, List<List<Object>> values) throws IOException {
        UpdateValuesResponse resp = service.spreadsheets().values().update(spreadsheetId, range, new ValueRange().setValues(values))
                .setValueInputOption("USER_ENTERED")
                .execute();
        return resp.getUpdatedCells();
    }

    public List<String> getRowNumber() throws IOException {
        Spreadsheet s = service.spreadsheets().get(spreadsheetId).execute();
        System.out.println(s.getSheets());
        return null;
    }

}
