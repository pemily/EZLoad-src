package com.pascal.bientotrentier.exporter;

import com.google.api.services.sheets.v4.Sheets;
import com.pascal.bientotrentier.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.exporter.ezPortfolio.MesOperations;
import com.pascal.bientotrentier.exporter.ezPortfolio.MonPortefeuille;
import com.pascal.bientotrentier.gdrive.GDriveConnection;
import com.pascal.bientotrentier.gdrive.GDriveSheets;
import com.pascal.bientotrentier.gdrive.SheetValues;
import com.pascal.bientotrentier.sources.Reporting;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class EZPortfolioManager {

    private Reporting reporting;
    private GDriveSheets sheets;

    public EZPortfolioManager(Reporting reporting, EZPortfolioSettings settings) throws GeneralSecurityException, IOException {
        Sheets service = GDriveConnection.getService(settings.getGdriveCredsFile());
        sheets = new GDriveSheets(reporting, service, settings.getEzPortfolioId());
        this.reporting = reporting;
    }

    public EZPortfolio load() throws Exception {
        try(Reporting rep = reporting.pushSection("Loading EZPortfolio...")){
            reporting.info("Getting data from Google Drive API...");
            EZPortfolio ezPortfolio = new EZPortfolio();

            List<SheetValues> ezSheets = sheets.batchGet("MesOperations!A2:K", "MonPortefeuille!A4:L");

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
    }

    public void save(EZPortfolio ezPortfolio) throws Exception {
        reporting.info("Saving EZPortfolio...");
        MesOperations operations = ezPortfolio.getMesOperations();
        int firstFreeRow = operations.getFirstFreeRow()+1; // +1 to add the header
        // sheets.update("MesOperations!A"+firstFreeRow+":J", operations.getNewOperations());

        sheets.batchUpdate(
                new SheetValues("MesOperations!A"+firstFreeRow+":K", operations.getNewOperations()),
                ezPortfolio.getMonPortefeuille().getSheetValues());

        reporting.info("Save done!");
    }
}
