package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class EZPorfolioProxyV5 implements EZPortfolioProxy {

    private Reporting reporting;
    private GDriveSheets sheets;
    private EZPortfolio ezPortfolio;

    public EZPorfolioProxyV5(Reporting reporting, GDriveSheets sheets){
        this.reporting = reporting;
        this.sheets = sheets;
    }

    @Override
    public int getEzPortfolioVersion() {
        return 5;
    }

    @Override
    public void load() throws Exception {
        ezPortfolio = new EZPortfolio("v5");

        List<SheetValues> ezSheets = sheets.batchGet("MesOperations!A2:K", "MonPortefeuille!A4:L");

        SheetValues allOperations = ezSheets.get(0);
        MesOperations mesOperations = new MesOperations(reporting, allOperations);
        ezPortfolio.setMesOperations(mesOperations);
        reporting.info(allOperations.getValues().size() + " rows from MesOperations loaded.");

        SheetValues portefeuille = ezSheets.get(1);
        MonPortefeuille monPortefeuille = new MonPortefeuille(reporting, portefeuille);
        ezPortfolio.setMonPortefeuille(monPortefeuille);
        reporting.info(portefeuille.getValues().size() + " rows from MonPortefeuille loaded.");
    }

    @Override
    // return the list of EzEdition operation not saved
    public List<EzReport> save(List<EzReport> operationsToAdd) throws Exception {
        reporting.info("Saving EZPortfolio...");
        MesOperations operations = ezPortfolio.getMesOperations();
        int firstFreeRow = operations.getFirstFreeRow()+1; // +1 to add the header

        List<EzReport> notSaved = new LinkedList<>();
        boolean errorFound = false;
        AtomicInteger nbOperationSaved = new AtomicInteger();
        for (EzReport ezReportToAdd : operationsToAdd){
            if (ezReportToAdd.getErrors().size() > 0) errorFound = true;
            if (errorFound){
                // if one of the operations is in error the report will be also in error (see the setter of the EzEdition in EzReport)
                notSaved.add(ezReportToAdd);
            }
            else {
                ezReportToAdd.getEzEditions()
                        .forEach(ezEdition -> {
                            operations.newOperation(ezEdition.getEzOperationEdition());
                            nbOperationSaved.incrementAndGet();
                        });
            }
        }

        if (operations.getNewOperations().size() > 0) {
            reporting.info("Saving "+nbOperationSaved.get()+" operations");
            sheets.update("MesOperations!A" + firstFreeRow + ":J", operations.getNewOperations());
            sheets.batchUpdate(
                    new SheetValues("MesOperations!A" + firstFreeRow + ":", operations.getNewOperations()),
                    ezPortfolio.getMonPortefeuille().getSheetValues());
        }

        reporting.info("Save done!");
        return notSaved;
    }

    @Override
    public Optional<EZDate> getLastOperationDate(EnumEZBroker courtier, EZAccountDeclaration account) {
        return ezPortfolio.getMesOperations().getLastOperationDate(courtier, account);
    }

    @Override
    public boolean isFileAlreadyLoaded(EnumEZBroker courtier, EZAccountDeclaration account, EZDate pdfDate) {
        return ezPortfolio.getMesOperations().isFileAlreadyLoaded(courtier, account, pdfDate);
    }

    @Override
    public boolean isOperationsExists(Row operation) {
        return ezPortfolio.getMesOperations().isOperationsExists(operation);
    }

    @Override
    public Optional<Row> searchPortefeuilleRow(String valeur) {
        return ezPortfolio.getMonPortefeuille().searchRow(valeur);
    }

    @Override
    public Row getNewPortefeuilleRow(String valeur) {
        return ezPortfolio.getMonPortefeuille().getNewRow(valeur);
    }


    public static boolean isCompatible(Reporting reporting, GDriveSheets sheets) {
        try(Reporting rep = reporting.pushSection("Vérification de la version d'EZPortfolio avec EZLoad")){

            // en V4 la colonne MesOperations.Periode existe, elle a été renommé en "Quantité" en V5
            SheetValues s;
            try {
                s = sheets.getCells("MesOperations!D1:D1"); // récupère la cellule de la colonne D ligne 1 de MesOperations
            }
            catch(Exception e){
                // TODO catcher l'exception sur le token expired
                String errorMsg = "Il y a un problème pour se connecter à EzPortfolio ou alors vous devez recréer votre fichier de sécurité. "+sheets.getEzPortfolioUrl();
                reporting.error(errorMsg);
                throw new IllegalStateException(errorMsg, e);
            }

            reporting.info("Valeur trouvée: "+ s.getValues().get(0).getValueStr(0));
            return s.getValues().get(0).getValueStr(0).equals("Quantité");
        }
        catch(Exception e){
            if (e instanceof IllegalStateException){
                throw e;
            }
            reporting.error("Il ne s'agit pas de EZPortfolio V5 ou il y a eu un problème", e);
            return false;
        }
    }

}