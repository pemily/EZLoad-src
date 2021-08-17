package com.pascal.bientotrentier.sources.bourseDirect.transform;

import com.pascal.bientotrentier.parsers.bourseDirect.BourseDirectPdfParser;
import com.pascal.bientotrentier.parsers.bourseDirect.Dataset;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.bientotrentier.util.BRException;
import com.pascal.bientotrentier.util.BRParsingException;
import com.pascal.bientotrentier.util.StringUtils;

import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class BourseDirectText2Model {


    public static final String TEXT_INTRO = "Nous vous prions de trouver ci-dessous votre relevé d'opérations. Sans observation de votre part au\n" +
            "sujet du présent relevé, nous le considérerons comme ayant obtenu votre accord. Veuillez agréer nos\n" +
            "salutations distinguées.\n";

    public static final String DATE_DÉSIGNATION_DÉBIT = "Date Désignation Débit (";
    public static final String FOOTER = "Sous réserve de bonne fin / Ce relevé ne constitue pas une facture";

    private Reporting reporting;

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
            BourseDirectModel model = new BourseDirectModel();
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

        String section[] = pdfText.split(TEXT_INTRO);
        String accountSection[] = section[0].split("[\\n]");
        model.setAccountOwnerName(accountSection[2].trim());

        String textDebutTableau = DATE_DÉSIGNATION_DÉBIT;
        String section2[] = section[1].split(Pattern.quote(textDebutTableau));
        String section2Splitted[] = section2[0].split("[\\n]");
        String leDate = section2Splitted[0]; // Le 24/02/2021
        if (!leDate.startsWith("Le ")) {
            throw new BRException("Invalid Format detected.");
        }
        model.setDateAvisOperation(leDate.substring(3).trim());
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
            model.setDates(dataset.getDates());
            model.setAmounts(dataset.getAmounts());
        }
        catch(Exception e){
            BRParsingException parsingException = new BRParsingException(e);
            parsingException.setAnalyzedText(dataSetStr);
            throw parsingException;
        }
    }
}
