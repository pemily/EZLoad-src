package com.pascal.bientotrentier.exporter;

import com.google.api.services.sheets.v4.Sheets;
import com.pascal.bientotrentier.gdrive.GDriveConnection;
import com.pascal.bientotrentier.gdrive.GDriveSheets;
import com.pascal.bientotrentier.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.exporter.ezPortfolio.MesOperations;
import com.pascal.bientotrentier.sources.Reporting;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class EZPortfolioHandler {

    private Reporting reporting;
    private GDriveSheets sheets;

    public EZPortfolioHandler(Reporting reporting, EZPortfolioSettings settings) throws GeneralSecurityException, IOException {
        Sheets service = GDriveConnection.getService(settings.getgDriveCredentialsFile());
        sheets = new GDriveSheets(reporting, service, settings.getEzPortfolioId());
        this.reporting = reporting;
    }

    public EZPortfolio load() throws Exception {
        reporting.pushSection("Loading EZPortfolio...");
        try {
            EZPortfolio ezPortfolio = new EZPortfolio();

            List<List<Object>> allOperations = sheets.getCells("MesOperations!A2:J");
            MesOperations mesOperations = new MesOperations(allOperations);
            ezPortfolio.setMesOperations(mesOperations);
            reporting.info(allOperations.size()+" operations loaded.");
            return ezPortfolio;
        }
        finally {
            reporting.popSection();
        }
    }

    public void save(EZPortfolio ezPortfolio) throws Exception {
        reporting.pushSection("saving EZPortfolio...");
        try {
            MesOperations operations = ezPortfolio.getMesOperations();
            int firstFreeRow = operations.getFirstFreeRow()+1; // +1 to add the header
            sheets.update("MesOperations!A"+firstFreeRow+":J", operations.getNewOperations());
        }
        finally {
            reporting.popSection();
        }
    }
}
