package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.parsers.bourseDirect.BourseDirectPdfParser;
import com.pascal.ezload.service.parsers.bourseDirect.Dataset;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.ezload.service.util.BRException;
import com.pascal.ezload.service.util.BRParsingException;
import com.pascal.ezload.service.util.StringUtils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public BourseDirectModel toModel(String pdfText) {
        BourseDirectModel model;
        reporting.info("BourseDirect model transformation");
        model = analyzePdfText(pdfText);
        reporting.info("BourseDirect model transformation => ok");
        return model;
    }

    private BourseDirectModel analyzePdfText(String pdfText) throws BRException {
        try {
            pdfText = StringUtils.clean(pdfText);
            BourseDirectModel model = new BourseDirectModel(VERSION);
            analyzePdfText(pdfText, model);
            return model;
        }
        catch (BRParsingException e){
            e.setFileContent(pdfText);
            throw e;
        }
        catch(Exception e){
            BRParsingException parsingException = new BRParsingException(e);
            parsingException.setFileContent(pdfText);
            throw parsingException;
        }
    }


    private void analyzePdfText(String pdfText, BourseDirectModel model) {

        String[] section = pdfText.split(TEXT_INTRO);
        String[] accountSection = section[0].split("[\\n]");
        model.setAccountOwnerName(accountSection[2].trim());

        String[] section2 = section[1].split(Pattern.quote(DATE_DESIGNATION_DEBIT));
        String[] section2Splitted = section2[0].split("[\\n]");
        String leDate = section2Splitted[0]; // Le 24/02/2021
        if (!leDate.startsWith("Le ")) {
            throw new BRException("Invalid Format detected.");
        }
        model.setDateAvisOperation(EZDate.parseFrenchDate(leDate.substring(3).trim(), '/'));
        String [] accountDetails = accountSection[1].split(" ");
        model.setAccountNumber(accountDetails[2]);
        model.setAccountType(accountDetails[3]);

        List<String> addr = new LinkedList<>(Arrays.asList(section2Splitted));
        addr.remove(0);
        model.setAddress(String.join("\n", addr).trim());

        String[] deviseResearch = section2[1].split("[\\(\\)]");
        model.setDeviseDebit(deviseResearch[0].trim());
        model.setDeviseCredit(deviseResearch[2].trim());

        String prepareDataSet = StringUtils.divide(section2[1], model.getDeviseDebit() + ") Crédit (" + model.getDeviseCredit() + ")")[1];
        String dataSetStr = StringUtils.divide(prepareDataSet, FOOTER)[0].trim();

        try {
            BourseDirectPdfParser parser = new BourseDirectPdfParser(new StringReader(dataSetStr));
            Dataset dataset = parser.Dataset();
            model.setOperations(dataset.getOperations());
            // the dates in the BourseDirect pdf are french dates: dd/mm/yyyy
            model.setDates(new ArrayList<>(dataset.getDates().stream().map(d -> EZDate.parseFrenchDate(d, '/')).collect(Collectors.toList())));
            model.setAmounts(dataset.getAmounts());
        }
        catch(Exception e){
            BRParsingException parsingException = new BRParsingException(e);
            parsingException.setAnalyzedText(dataSetStr);
            throw parsingException;
        }
    }
}
