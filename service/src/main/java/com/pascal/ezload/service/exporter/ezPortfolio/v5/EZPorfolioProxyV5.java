package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.*;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EZPorfolioProxyV5 implements EZPortfolioProxy {

    private static final int FIRST_ROW_MON_PORTEFEUILLE = 4;
    private static final int FIRST_ROW_PRU = 5;
    private static final int FIRST_ROW_MES_OPERATIONS = 1;

    private GDriveSheets sheets;
    private EZPortfolio ezPortfolio;

    public EZPorfolioProxyV5(GDriveSheets sheets){
        this.sheets = sheets;
    }

    @Override
    public int getEzPortfolioVersion() {
        return 5;
    }

    @Override
    public void load(Reporting reporting) throws Exception {
        ezPortfolio = new EZPortfolio("v5");

        List<SheetValues> ezSheets = sheets.batchGet(reporting,
                                            "MesOperations!A"+FIRST_ROW_MES_OPERATIONS+":K",
                                                    "MonPortefeuille!A"+FIRST_ROW_MON_PORTEFEUILLE+":L",
                                                    "PRU!A"+FIRST_ROW_PRU+":A");

        SheetValues allOperations = ezSheets.get(0);
        MesOperations mesOperations = new MesOperations(allOperations);
        ezPortfolio.setMesOperations(mesOperations);
        reporting.info(allOperations.getValues().size() + " rows from MesOperations loaded.");

        SheetValues portefeuille = ezSheets.get(1);
        MonPortefeuille monPortefeuille = new MonPortefeuille(portefeuille);
        ezPortfolio.setMonPortefeuille(monPortefeuille);
        reporting.info(portefeuille.getValues().size() + " rows from MonPortefeuille loaded.");

        SheetValues allPRUs = ezSheets.get(2);
        PRU pru = new PRU(allPRUs);
        ezPortfolio.setPru(pru);
        reporting.info(allPRUs.getValues().size() + " rows from PRU loaded.");
    }

    @Override
    // return the list of EzEdition operation not saved
    public List<EzReport> save(Reporting reporting, List<EzReport> operationsToAdd) throws Exception {
        reporting.info("Saving EZPortfolio...");
        MesOperations operations = ezPortfolio.getMesOperations();

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
                            if (ezEdition.getEzOperationEditions() != null) {
                                ezEdition.getEzOperationEditions()
                                        .forEach(ezOperation -> {
                                            operations.newOperation(ezEdition.getData(), ezOperation);
                                            nbOperationSaved.incrementAndGet();
                                        });

                                ezEdition.getEzPortefeuilleEditions().forEach(ezPortefeuilleEdition ->
                                        ezPortfolio.getMonPortefeuille().apply(ezPortefeuilleEdition));
                            }
                            else if (!ezEdition.getEzPortefeuilleEditions().isEmpty()){
                                throw new IllegalStateException("Il y a une opération sur le portefeuille qui n'est pas déclarée dans la liste des Opérations. le problème est avec la règle: "
                                        +ezEdition.getRuleDefinitionSummary().getBroker()+"_v"+ezEdition.getRuleDefinitionSummary().getBrokerFileVersion()
                                        +ezEdition.getRuleDefinitionSummary().getName());
                            }
                        });
            }
        }

        if (operations.getNewOperations().size() > 0) {
            reporting.info("Saving "+nbOperationSaved.get()+" operations");
            SheetValues monPortefeuille = ezPortfolio.getMonPortefeuille().getSheetValues();
            sheets.batchUpdate(reporting,
                    new SheetValues("MesOperations!A" + (operations.getNbOfExistingOperations()+FIRST_ROW_MES_OPERATIONS) + ":K",
                                operations.getNewOperations()),
                    monPortefeuille,
                    new SheetValues("PRU!A"+(ezPortfolio.getPru().getNumberOfExistingPRUs()+FIRST_ROW_PRU) + ":A",
                                ezPortfolio.getPru().getNewPRUs()));
            ezPortfolio.getMesOperations().saveDone();
            ezPortfolio.getPru().saveDone();
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
    public void applyOnPortefeuille(EzPortefeuilleEdition ezPortefeuilleEdition) {
        ezPortfolio.getMonPortefeuille().apply(ezPortefeuilleEdition);
    }

    @Override
    public void fillFromMonPortefeuille(EzData data, String tickerCode) {
        ezPortfolio.getMonPortefeuille().fill(data, tickerCode);
    }

    @Override
    public Set<ShareValue> getShareValues() {
        return ezPortfolio.getMonPortefeuille().getShareValues();
    }

    @Override
    public PRU getPRU() {
        return ezPortfolio.getPru();
    }

    @Override
    public List<String> getNewPRUValues() {
        return ezPortfolio.getPru().getNewPRUValues();
    }

    @Override
    public EZPortfolioProxy createDeepCopy() {
        EZPorfolioProxyV5 copy = new EZPorfolioProxyV5(sheets);
        copy.ezPortfolio = this.ezPortfolio.createDeepCopy();
        return copy;
    }

    public static boolean isCompatible(Reporting reporting, GDriveSheets sheets) throws Exception {
        try(Reporting rep = reporting.pushSection("Vérification de la version d'EZPortfolio avec EZLoad")){

            // en V4 la colonne MesOperations.Periode existe, elle a été renommé en "Quantité" en V5
            SheetValues s = sheets.getCells(reporting,"MesOperations!D1:D1"); // récupère la cellule de la colonne D ligne 1 de MesOperations
            reporting.info("Valeur trouvée: "+ s.getValues().get(0).getValueStr(0));
            return s.getValues().get(0).getValueStr(0).equals("Quantité");
        }
    }

}
