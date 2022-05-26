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
package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.UnmergeCellsRequest;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.*;
import com.pascal.ezload.service.exporter.ezEdition.data.common.AccountData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.ReportData;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;
import com.pascal.ezload.service.util.Sleep;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EZPorfolioProxyV5 implements EZPortfolioProxy {

    public static final int FIRST_ROW_MON_PORTEFEUILLE = 4;
    public static final int FIRST_ROW_PRU = 5;
    public static final int FIRST_ROW_MES_OPERATIONS = 1;

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
                                            "MesOperations!A"+FIRST_ROW_MES_OPERATIONS+":L",
                                                    "MonPortefeuille!A"+FIRST_ROW_MON_PORTEFEUILLE+":AN",
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
    public List<EzReport> save(EzProfil profil, Reporting reporting, List<EzReport> operationsToAdd, List<String> ignoreEzEditionId) throws Exception {
        reporting.info("Saving EZPortfolio...");
        MesOperations operations = ezPortfolio.getMesOperations();

        List<EzReport> notSaved = new LinkedList<>();
        boolean errorFound = false;
        AtomicInteger nbOperationSaved = new AtomicInteger();
        AtomicInteger nbPortefeuilleRowSaved = new AtomicInteger();
        for (EzReport ezReportToAdd : operationsToAdd){
            if (ezReportToAdd.getErrors().size() > 0) errorFound = true;
            if (errorFound){
                notSaved.add(ezReportToAdd);
            }
            else {
                ezReportToAdd.getEzEditions()
                        .forEach(ezEdition -> {
                            if (ignoreEzEditionId.contains(ezEdition.getId())){
                                EzOperationEdition ignoredOperation = new EzOperationEdition();
                                ignoredOperation.setDescription("Operation Ignorée");
                                String sourceFile = ezEdition.getData().get(ReportData.report_source);
                                String accountNumber = ezEdition.getData().get(AccountData.account_number);
                                Optional<EnumEZBroker> broker = profil.getBourseDirect().getAccounts().stream().anyMatch(a -> a.getNumber() != null && a.getNumber().equals(accountNumber)) ? Optional.of(EnumEZBroker.BourseDirect) : Optional.empty();
                                ignoredOperation.setBroker(broker.map(EnumEZBroker::getEzPortfolioName).orElse(null));
                                ignoredOperation.setDate(BourseDirectDownloader.getDateFromPdfFilePath(sourceFile).toEzPortoflioDate());
                                RuleDefinitionSummary ruleDefinitionSummary = new RuleDefinitionSummary();
                                ruleDefinitionSummary.setBroker(broker.orElse(null));
                                ruleDefinitionSummary.setName("IGNOREE");
                                operations.newOperation(ezEdition.getData(), ignoredOperation, ruleDefinitionSummary);
                                nbOperationSaved.incrementAndGet();
                            }
                            else {
                                if (ezEdition.getErrors().size() > 0){
                                    // if an operation is in error, but not ignored, stop the process
                                    throw new IllegalStateException("Il y a une opération en erreur, vous devez la corriger ou l'ignorer. Voir le rapport: "+ezEdition.getData().get(ReportData.report_source));
                                }
                                else if (ezEdition.getEzOperationEditions() != null) {
                                    ezEdition.getEzOperationEditions()
                                            .forEach(ezOperation -> {
                                                operations.newOperation(ezEdition.getData(), ezOperation, ezEdition.getRuleDefinitionSummary());
                                                nbOperationSaved.incrementAndGet();
                                            });

                                    ezEdition.getEzPortefeuilleEditions().forEach(ezPortefeuilleEdition -> {
                                        ezPortfolio.getMonPortefeuille().apply(ezPortefeuilleEdition);
                                        nbPortefeuilleRowSaved.incrementAndGet();
                                    });
                                } else if (!ezEdition.getEzPortefeuilleEditions().isEmpty()) {
                                    throw new IllegalStateException("Il y a une opération sur le portefeuille qui n'est pas déclarée dans la liste des Opérations. le problème est avec la règle: "
                                            + ezEdition.getRuleDefinitionSummary().getBroker() + "_v" + ezEdition.getRuleDefinitionSummary().getBrokerFileVersion()
                                            + ezEdition.getRuleDefinitionSummary().getName());
                                }
                            }
                        });
            }
        }

        if (nbOperationSaved.get() > 0 || nbPortefeuilleRowSaved.get() > 0) {
            reporting.info("Export de "+nbOperationSaved.get()+" operations et "+nbPortefeuilleRowSaved.get()+" lignes du portefeuille");
            SheetValues monPortefeuille = ezPortfolio.getMonPortefeuille().getSheetValues();

            fixBugUnmergedMesOperationCells();

            // Force la mise a jours des methods GoogleFinance dans les cellulles N&O de MonPortefeuille avant la mise a jour de toutes les autres opérations. (sinon ca bug)
            fixGoogleFinanceBug(sheets, reporting, monPortefeuille);
            // attend un peu pour etre sur que ce soit fait
            Sleep.waitSeconds(5);
            sheets.batchUpdate(reporting,
                    SheetValues.createFromRowLists("MesOperations!A" + (operations.getNbOfExistingOperations()+FIRST_ROW_MES_OPERATIONS) + ":L",
                                operations.getNewOperations()),
                    monPortefeuille,
                    SheetValues.createFromRowLists("PRU!A"+(ezPortfolio.getPru().getNumberOfExistingPRUs()+FIRST_ROW_PRU) + ":A",
                                ezPortfolio.getPru().getNewPRUs()));
            ezPortfolio.getMesOperations().saveDone();
            ezPortfolio.getPru().saveDone();
        }

        reporting.info("Export terminé!");
        return notSaved;
    }

    private void fixBugUnmergedMesOperationCells() throws IOException {
        UnmergeCellsRequest unmerge = new UnmergeCellsRequest();
        GridRange gridRange = new GridRange();
            /* https://docs.rs/google-sheets4/1.0.14+20200630/google_sheets4/struct.GridRange.html
            https://googlesheets4.tidyverse.org/articles/range-specification.html
            For example, if "Sheet1" is sheet ID 0, (le 1er onglet est toujours egale a 0, les suivant sont de vrai id)
             then:
            Sheet1!A1:A1 == sheet_id: 0, start_row_index: 0, end_row_index: 1, start_column_index: 0, end_column_index: 1
            Sheet1!A3:B4 == sheet_id: 0, start_row_index: 2, end_row_index: 4, start_column_index: 0, end_column_index: 2
            Sheet1!A:B == sheet_id: 0, start_column_index: 0, end_column_index: 2
            Sheet1!A5:B == sheet_id: 0, start_row_index: 4, start_column_index: 0, end_column_index: 2
            Sheet1 == sheet_id:0
             */

        // MesOperations:I66:L66
        gridRange.setSheetId(sheets.getSheetId("MesOperations")); https://stackoverflow.com/questions/52934537/how-to-use-sheet-id-in-google-sheets-api/66575843#66575843
            /* cellule a unmergé
                MesOpérations:I66:L66
                MesOpérations:I108:L108
                MesOpérations:I169:L169
                MesOpérations:I182:L182
                MesOpérations:I194:L194
                MesOpérations:I201:L201
            */

        gridRange.setStartColumnIndex(8);
        gridRange.setEndColumnIndex(13);
        gridRange.setStartRowIndex(65);
        gridRange.setEndRowIndex(202);
        unmerge.setRange(gridRange);
        Request r = new Request();
        r.setUnmergeCells(unmerge);
        sheets.applyRequest(Arrays.asList(r));
    }

    private void fixGoogleFinanceBug(GDriveSheets sheets, Reporting reporting, SheetValues monPortefeuille) throws Exception {
        SheetValues.CellXY[] range = monPortefeuille.getCellsRange();
        List<Row> googleFinanceFcts = monPortefeuille.getValues().stream()
                                                    .map(r -> {
                                                        Row newRow = new Row(r.getRowNumber());
                                                        newRow.setValue(0, r.getValueStr(MonPortefeuille.MONNAIE_COL));
                                                        newRow.setValue(1, r.getValueStr(MonPortefeuille.COURS_VALEUR_COL));
                                                        return newRow;
                                                    }).collect(Collectors.toList());
        sheets.update(reporting, "MonPortefeuille!N"+range[0].getRowNumber()+":O"+range[1].getRowNumber(), googleFinanceFcts);

        UnmergeCellsRequest unmerge = new UnmergeCellsRequest();

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

    @Override
    public Optional<EzPortefeuilleEdition> createNoOpEdition(String ticker) {
        return ezPortfolio.getMonPortefeuille().createFrom(ticker);
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
