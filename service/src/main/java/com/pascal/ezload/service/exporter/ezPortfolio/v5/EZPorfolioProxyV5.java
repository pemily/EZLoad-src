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
import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;
import com.pascal.ezload.service.util.Sleep;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EZPorfolioProxyV5 implements EZPortfolioProxy {

    public static final int FIRST_ROW_MON_PORTEFEUILLE = 4;
    public static final int FIRST_ROW_PRU = 5;
    public static final int FIRST_ROW_MES_OPERATIONS = 1;
    public static final int FIRST_ROW_EZLOAD_SHARES = 1;

    private static final String MesOperationsSheet = "MesOperations";
    private static final String MonPortefeuilleSheet = "MonPortefeuille";
    private static final String EZLoadSharesSheet = "EZLoadActions";
    private static final String MaPerformanceSheet = "MaPerformance";
    private static final String PRUSheet = "PRU";

    private final GDriveSheets sheets;
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

        sheets.createSheet(EZLoadSharesSheet);

        List<SheetValues> ezSheets = sheets.batchGet(reporting,
                                                    EZLoadSharesSheet +"!A"+FIRST_ROW_EZLOAD_SHARES+":F",
                                                    MesOperationsSheet +"!A"+FIRST_ROW_MES_OPERATIONS+":L",
                                                    MonPortefeuilleSheet +"!A"+FIRST_ROW_MON_PORTEFEUILLE+":AN",
                                                    PRUSheet +"!A"+FIRST_ROW_PRU+":A",
                                                    MaPerformanceSheet+"!C4:C4");

        SheetValues allEZLoadShares = ezSheets.get(0);
        EZLoadShareSheet ezLoadShareSheet = new EZLoadShareSheet(allEZLoadShares);
        ezLoadShareSheet.addHeaderIfMissing();
        ezPortfolio.setEzLoadShareSheet(ezLoadShareSheet);
        reporting.info(allEZLoadShares.getValues().size() + " rows from "+ EZLoadSharesSheet +" loaded.");

        SheetValues allOperations = ezSheets.get(1);
        MesOperations mesOperations = new MesOperations(allOperations);
        ezPortfolio.setMesOperations(mesOperations);
        reporting.info(allOperations.getValues().size() + " rows from "+ MesOperationsSheet +" loaded.");

        SheetValues portefeuille = ezSheets.get(2);
        MonPortefeuille monPortefeuille = new MonPortefeuille(portefeuille);
        ezPortfolio.setMonPortefeuille(monPortefeuille);
        reporting.info(portefeuille.getValues().size() + " rows from "+ MonPortefeuilleSheet +" loaded.");

        SheetValues allPRUs = ezSheets.get(3);
        PRU pru = new PRU(allPRUs);
        reporting.info(allPRUs.getValues().size() + " rows from "+ PRUSheet +" loaded.");

        SheetValues performance = ezSheets.get(4);
        MaPerformance maPerformance = new MaPerformance(performance);
        ezPortfolio.setMaPerformance(maPerformance);
        reporting.info(performance.getValues().size() + " rows from "+ MaPerformanceSheet +" loaded.");
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
                                ignoredOperation.setDate(BourseDirectDownloader.getDateFromFilePath(sourceFile).toEzPortoflioDate());
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

                                    ezPortfolio.getMaPerformance().updateWith(ezEdition.getEzMaPerformanceEdition());

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

            EZLoadShareSheet ezLoadSheetShareValues = ezPortfolio.getEZLoadShareSheet();

            // Update all the sheets of EZPortfolio in one call
            sheets.batchUpdate(reporting,
                    SheetValues.createFromRowLists(EZLoadSharesSheet+"!A" + (ezLoadSheetShareValues.getNbOfExistingShareValues()+FIRST_ROW_EZLOAD_SHARES) + ":E",
                            ezLoadSheetShareValues.getNewShareValues()),
                    SheetValues.createFromRowLists(MesOperationsSheet +"!A" + (operations.getNbOfExistingOperations()+FIRST_ROW_MES_OPERATIONS) + ":L",
                                operations.getNewOperations()),
                    monPortefeuille,
                    ezPortfolio.getMaPerformance().getSheetValues()
            );

            ezPortfolio.getMesOperations().saveDone();
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
        gridRange.setSheetId(sheets.getSheetId(MesOperationsSheet));
            /* https://stackoverflow.com/questions/52934537/how-to-use-sheet-id-in-google-sheets-api/66575843#66575843
               cellule a unmergé
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
        sheets.applyRequest(List.of(r));
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
        sheets.update(reporting, MonPortefeuilleSheet +"!N"+range[0].getRowNumber()+":O"+range[1].getRowNumber(), googleFinanceFcts);
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
    public void fillFromMonPortefeuille(EzData data, String tickerCode, String accountType, EnumEZBroker broker) {
        ezPortfolio.getMonPortefeuille().fill(data, tickerCode, accountType, broker);
    }

    @Override
    public Set<ShareValue> getShareValuesFromMonPortefeuille() {
        return ezPortfolio.getMonPortefeuille().getShareValues();
    }

    @Override
    public Optional<EZAction> findShareByIsin(String isin) {
        return ezPortfolio.getEZLoadShareSheet().findByIsin(isin);
    }

    @Override
    public EZPortfolioProxy createDeepCopy(List<EZAction> newShares) {
        EZPorfolioProxyV5 copy = new EZPorfolioProxyV5(sheets);
        copy.ezPortfolio = this.ezPortfolio.createDeepCopy(newShares);
        return copy;
    }

    @Override
    public Optional<EzPortefeuilleEdition> createNoOpEdition(ShareValue ticker) {
        return ezPortfolio.getMonPortefeuille().createFrom(ticker);
    }

    @Override
    public String getEzLiquidityName(String ezAccountType, EnumEZBroker broker) {
        return getShareValuesFromMonPortefeuille()
                .stream()
                .filter(s -> s.getTickerCode().equals(ShareValue.LIQUIDITY_CODE)
                        && s.getBroker().equals(broker)
                        && s.getEzAccountType().equals(ezAccountType))
                .findFirst()
                .map(ShareValue::getUserShareName)
                .orElse(new ShareValue(ShareValue.LIQUIDITY_CODE, "", ezAccountType, broker, "").getUserShareName());
    }

    @Override
    public void newAction(EZAction v) {
        ezPortfolio.getEZLoadShareSheet().newShareValue(v);
    }

    @Override
    public List<EZAction> getNewShares() {
        return ezPortfolio.getEZLoadShareSheet().getNewShareValues()
                .stream()
                .filter(EZLoadShareSheet::isNotHeader)
                .map(EZLoadShareSheet::row2EZAction).collect(Collectors.toList());
    }

    @Override
    public void updateNewShare(EZAction shareValue) {
        ezPortfolio.getEZLoadShareSheet().updateNewShareValue(shareValue);
    }

    @Override
    public void applyOnPerformance(EzPerformanceEdition ezPerformanceEdition) {
        ezPortfolio.getMaPerformance().updateWith(ezPerformanceEdition);
    }

    public static boolean isCompatible(Reporting reporting, GDriveSheets sheets) throws Exception {
        try(Reporting rep = reporting.pushSection("Vérification de la version d'EZPortfolio avec EZLoad")){

            // en V4 la colonne MesOperations.Periode existe, elle a été renommé en "Quantité" en V5
            SheetValues s = sheets.getCells(reporting, MesOperationsSheet +"!D1:D1"); // récupère la cellule de la colonne D ligne 1 de MesOperations
            reporting.info("Valeur trouvée: "+ s.getValues().get(0).getValueStr(0));
            return s.getValues().get(0).getValueStr(0).equals("Quantité");
        }
    }

}
