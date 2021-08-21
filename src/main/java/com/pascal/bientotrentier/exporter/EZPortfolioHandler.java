package com.pascal.bientotrentier.exporter;

import com.google.api.services.sheets.v4.Sheets;
import com.pascal.bientotrentier.exporter.ezPortfolio.MonPortefeuille;
import com.pascal.bientotrentier.gdrive.GDriveConnection;
import com.pascal.bientotrentier.gdrive.GDriveSheets;
import com.pascal.bientotrentier.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.exporter.ezPortfolio.MesOperations;
import com.pascal.bientotrentier.gdrive.Row;
import com.pascal.bientotrentier.gdrive.SheetValues;
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

            List<SheetValues> ezSheets = sheets.batchGet("MesOperations!A2:J", "MonPortefeuille!A4:L");

            SheetValues allOperations = ezSheets.get(0);
            MesOperations mesOperations = new MesOperations(reporting, allOperations);
            ezPortfolio.setMesOperations(mesOperations);
            reporting.info(allOperations.getValues().size()+" rows from MesOperations loaded.");

            SheetValues portefeuille = ezSheets.get(1);
            MonPortefeuille monPortefeuille = new MonPortefeuille(reporting, portefeuille);
            ezPortfolio.setMonPortefeuille(monPortefeuille);
            reporting.info(portefeuille.getValues().size()+" rows from MonPortefeuille loaded.");
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
            // sheets.update("MesOperations!A"+firstFreeRow+":J", operations.getNewOperations());

            sheets.batchUpdate(
                    new SheetValues("MesOperations!A"+firstFreeRow+":J", operations.getNewOperations()),
                    ezPortfolio.getMonPortefeuille().getSheetValues());
        }
        finally {
            reporting.popSection();
        }
    }
}
