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
package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.common.util.*;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class BourseDirectText2Model {

    private static final int VERSION = 1;

    public static final String TEXT_INTRO = "Nous vous prions de trouver ci-dessous votre relevé d'opérations. Sans observation de votre part au\n" +
            "sujet du présent relevé, nous le considérerons comme ayant obtenu votre accord. Veuillez agréer nos\n" +
            "salutations distinguées.\n";

    public static final String DATE_DESIGNATION_DEBIT = "Date Désignation Débit (";
    public static final String FOOTER = "Sous réserve de bonne fin / Ce relevé ne constitue pas une facture";

    private final Reporting reporting;

    public BourseDirectText2Model(Reporting reporting){
        this.reporting = reporting;
    }

    public BourseDirectModel toModel(PdfTextExtractor.Result pdfText) {
        reporting.info("BourseDirect model transformation");
        BourseDirectModel model = analyzePdfText(pdfText);
        reporting.info("BourseDirect model transformation => ok");
        return model;
    }

    private BourseDirectModel analyzePdfText(PdfTextExtractor.Result pdfResult) throws BRException {
        try {
            BourseDirectModel model = new BourseDirectModel(VERSION);
            analyzePdfText(pdfResult, model);
            return model;
        }
        catch (BRParsingException e){
            e.setFileContent(pdfResult.getPdfText());
            throw e;
        }
        catch(Exception e){
            BRParsingException parsingException = new BRParsingException(e);
            parsingException.setFileContent(pdfResult.getPdfText());
            throw parsingException;
        }
    }


    private void analyzePdfText(PdfTextExtractor.Result pdfResult, BourseDirectModel model) {
        String pdfText = StringUtils.clean(pdfResult.getPdfText());

        String[] section = pdfText.split(TEXT_INTRO);
        String[] accountSection = section[0].split("[\\n]");
        model.setAccountOwnerName(accountSection[2].trim());

        String[] section2 = section[1].split(Pattern.quote(DATE_DESIGNATION_DEBIT));
        String[] section2Splitted = section2[0].split("[\\n]");
        String leDate = section2Splitted[0]; // Le 24/02/2021
        if (!leDate.startsWith("Le ")) {
            throw new BRException("Invalid Format detected.");
        }
        model.setDateAvisOperation(EZDate.parseFrenchDate(leDate.substring(3), '/'));
        String [] accountDetails = accountSection[1].split(" ");
        model.setAccountNumber(accountDetails[2]);
        model.setAccountType(accountDetails[3]);

        List<String> addr = new LinkedList<>(Arrays.asList(section2Splitted));
        addr.remove(0);
        model.setAddress(String.join("\n", addr).trim());

        String[] deviseResearch = section2[1].split("[\\(\\)]");
        model.setDeviseDebit(deviseResearch[0].trim());
        model.setDeviseCredit(deviseResearch[2].trim());

        float DateColX = 20;
        float DesignationColX = 80;
        float DebitColX = 380;
        float CreditColX = 470;
        float operationArrayStartY = 210;

        ArrayList<BourseDirectOperation> allOperations = new ArrayList<>();
        model.setOperations(allOperations);

        for (PdfTextExtractor.PositionText pt : pdfResult.getAllPositionTexts()){
            if (pt.getText().startsWith("Sous réserve de bonne fin"))
                break; // on a atteint la fin de la page

            if (pt.getY() > operationArrayStartY){
                // on est dans le tableau des opérations
                if (pt.isAfter(DateColX) && pt.isBefore(DesignationColX)){
                    // une nouvelle date d'opération => une nouvelle opération
                    BourseDirectOperation op = new BourseDirectOperation();
                    op.setPdfPositionDateY(pt.getY()-1);
                    op.setPdfPage(pt.getPage());
                    op.setDate(EZDate.parseFrenchDate(pt.getText(), '/'));
                    allOperations.add(op);
                }
                else if (pt.isAfter(DesignationColX) && pt.isBefore(DebitColX)) {
                    // dans la désignation
                    BourseDirectOperation op = findOrThrow(allOperations, pt);
                    String text[] = StringUtils.divide(pt.getText(), ':');
                    if (text == null) {
                        op.getOperationDescription().add(pt.getText());
                    }
                    else{
                        String charToReplace = "[. /\\\\,()]";
                        String key = text[0].replaceAll(charToReplace, " ")
                                .trim()
                                .replaceAll(charToReplace, "_")
                                .replaceAll("__", "_");
                        if (op.getFields().containsKey(key))
                            throw new UnsupportedOperationException("Field " + key + " already set. Unknown pdf format near the text: " + pt.getText());
                        op.getFields().put("ezOperation_"+key, NumberUtils.normalizeAmount(text[1].trim()));
                    }
                }
                else if (pt.isAfter(DebitColX) && pt.isBefore(CreditColX)){
                    // dans la colonne débit:
                    BourseDirectOperation op = findOrThrow(allOperations, pt);
                    if (op.getFields().containsKey("ezOperation_DEBIT")) throw new UnsupportedOperationException("Débit already set. Unknown pdf format near the text: "+pt.getText());
                    op.getFields().put("ezOperation_DEBIT", NumberUtils.normalizeAmount(pt.getText()));
                }
                else if (pt.isAfter(CreditColX)){
                    // dans la colonne credit:
                    BourseDirectOperation op = findOrThrow(allOperations, pt);
                    if (op.getFields().containsKey("ezOperation_CREDIT")) throw new UnsupportedOperationException("Crédit already set. Unknown pdf format near the text: "+pt.getText());
                    op.getFields().put("ezOperation_CREDIT", NumberUtils.normalizeAmount(pt.getText()));
                }
            }
        }
    }

    private BourseDirectOperation findOrThrow(ArrayList<BourseDirectOperation> allOps, PdfTextExtractor.PositionText textPosition){
        for (int i = 0; i < allOps.size(); i++){
            BourseDirectOperation op = allOps.get(i);
            if (textPosition.isBelow(op.getPdfPage(), op.getPdfPositionDateY()) || textPosition.sameY(op.getPdfPage(), op.getPdfPositionDateY())) {
                if (i == allOps.size() - 1) { // si c'est la derniere operation
                    return op;
                }

                BourseDirectOperation nextOp = allOps.get(i+1);
                if(textPosition.isAbove(nextOp.getPdfPage(), nextOp.getPdfPositionDateY())) { // si c'est avant l'operation suivante
                    return op;
                }
            }
        }
        throw new UnsupportedOperationException("Operation not found. Unknown pdf format near the text: "+textPosition.getText());
    }
}
