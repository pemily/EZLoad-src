package com.pascal.ezload.service.exporter.ezPortfolio.v4;

import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EnumEZCourtier;
import com.pascal.ezload.service.sources.Reporting;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class EZPorfolioProxyV4 implements EZPortfolioProxy {

    private Reporting reporting;
    private GDriveSheets sheets;
    private EZPortfolio ezPortfolio;

    public EZPorfolioProxyV4(Reporting reporting, GDriveSheets sheets){
        this.reporting = reporting;
        this.sheets = sheets;
    }

    @Override
    public void load() throws Exception {
        ezPortfolio = new EZPortfolio();

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
    public List<EzEdition> save(List<EzEdition> operationsToAdd) throws Exception {
        reporting.info("Saving EZPortfolio...");
        MesOperations operations = ezPortfolio.getMesOperations();
        int firstFreeRow = operations.getFirstFreeRow()+1; // +1 to add the header

        List<EzEdition> notSaved = new LinkedList<>();
        boolean errorFound = false;
        for (EzEdition ezOperationToAdd : operationsToAdd){
            if (ezOperationToAdd.getError() != null) errorFound = true;
            if (errorFound){
                notSaved.add(ezOperationToAdd);
            }
            else {
                operations.newOperation(ezOperationToAdd.getEzOperationEdition());
            }
        }

        if (operations.getNewOperations().size() > 0) {
            reporting.info("Saving "+operations.getNewOperations().size()+" operations");
            sheets.update("MesOperations!A" + firstFreeRow + ":J", operations.getNewOperations());
            sheets.batchUpdate(
                    new SheetValues("MesOperations!A" + firstFreeRow + ":", operations.getNewOperations()),
                    ezPortfolio.getMonPortefeuille().getSheetValues());
        }

        reporting.info("Save done!");
        return notSaved;
    }

    @Override
    public Optional<EZDate> getLastOperationDate(EnumEZCourtier courtier, EZAccountDeclaration account) {
        return ezPortfolio.getMesOperations().getLastOperationDate(courtier, account);
    }

    @Override
    public boolean isFileAlreadyLoaded(EnumEZCourtier courtier, EZAccountDeclaration account, EZDate pdfDate) {
        return ezPortfolio.getMesOperations().isFileAlreadyLoaded(courtier, account, pdfDate);
    }

    @Override
    public boolean isOperationsExists(EZOperation operation) {
        return ezPortfolio.getMesOperations().isOperationsExists(operation);
    }
}
