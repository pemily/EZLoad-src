package com.pascal.bientotrentier.bourseDirect.transform;

import com.pascal.bientotrentier.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.bientotrentier.parsers.bourseDirect.*;
import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.util.BRException;
import com.pascal.bientotrentier.util.BRParsingException;
import com.pascal.bientotrentier.util.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BourseDirectPdfAnalyser {

    public static final String TEXT_INTRO = "Nous vous prions de trouver ci-dessous votre relevé d'opérations. Sans observation de votre part au\n" +
            "sujet du présent relevé, nous le considérerons comme ayant obtenu votre accord. Veuillez agréer nos\n" +
            "salutations distinguées.\n";

    private static final Logger logger = Logger.getLogger(BourseDirectPdfAnalyser.class);
    public static final String DATE_DÉSIGNATION_DÉBIT = "Date Désignation Débit (";
    public static final String FOOTER = "Sous réserve de bonne fin / Ce relevé ne constitue pas une facture";

    private final BourseDirectSettings bourseDirectSettings;

    public BourseDirectPdfAnalyser(MainSettings mainSettings) {
        this.bourseDirectSettings = mainSettings.getBourseDirect();
    }

    public void start() throws IOException {
        try(Stream<Path> stream = Files.walk(Paths.get(this.bourseDirectSettings.getPdfOutputDir()), 5)){
            stream
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        logger.info("Decoding file: " + p.toFile().getAbsolutePath() + "...");
                        FileInputStream input = new FileInputStream(p.toFile());

                        PDDocument document = PDDocument.load(input);

                        PDFTextStripper stripper = new PDFTextStripper();
                        /*stripper.setParagraphStart("@@PStart@@");
                        stripper.setParagraphEnd("@@PEnd@@");
                        stripper.setWordSeparator("<@@>");*/

                        String pdfText = stripper.getText(document);
                        document.close();

                        // generateFileForTest(pdfText);

                        analyzePdfText(pdfText);
                    }
                    catch (BRParsingException e){
                        e.setFilePath(p.toFile().getAbsolutePath());
                        logger.error(e);
                    }
                    catch (IOException | BRException e) {
                        BRParsingException parsingException = new BRParsingException(e);
                        parsingException.setFilePath(p.toFile().getAbsolutePath());
                        logger.error(e);
                    }
                });
        }
    }

    private static int n = 1;
    private void generateFileForTest(String pdfText) throws IOException {
        Writer w = new FileWriter(new File("D:\\dev\\BientotRentier\\src\\test\\resources\\com\\pascal\\bientotrentier\\parsers\\bourseDirect\\avisOperation"+n+".txt"));
        n++;
        w.write(pdfText);
        w.flush();
        w.close();
    }

    public BourseDirectModel analyzePdfText(String pdfText) throws BRException {
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
            throw  parsingException;
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

        List<String> addr = new LinkedList<>(Arrays.asList(section2Splitted));
        addr.remove(0);
        model.setAddress(addr.stream().collect(Collectors.joining("\n")).trim());

        String deviseResearch[] = section2[1].split("[\\(\\)]");
        model.setDeviseDebit(deviseResearch[0].trim());
        model.setDeviseCredit(deviseResearch[2].trim());

        String prepareDataSet = StringUtils.divide(section2[1], model.getDeviseDebit() + ") Crédit (" + model.getDeviseCredit() + ")")[1];
        String dataSetStr = StringUtils.divide(prepareDataSet, FOOTER)[0].trim();

        try {
            BourseDirectPdfParser parser = new BourseDirectPdfParser(new StringReader(dataSetStr));
            // parser.setTracingEnabled(true);
            Dataset dataset = parser.Dataset();
            model.setOperations(dataset.getOperations());
            model.setDates(dataset.getDates());
            model.setAmounts(dataset.getAmounts());

            long nbOfDroitDeGarde = nbOfDroitsDeGarde(model.getOperations());

            // little check
            if (model.getDates().size() != model.getOperations().size()){
                throw new BRException("The number of dates found: "+model.getDates().size()+" do not match the number of operations found: "+model.getOperations().size()+" dates: "+model.getDates()+ " operations: "+model.getOperations());
            }
            if (model.getAmounts().size() != model.getOperations().size() - nbOfDroitDeGarde){
                throw new BRException("The number of amounts found: "+model.getAmounts().size()+" do not match the number of operations found: "+model.getOperations().size()+" amounts: "+model.getAmounts()+" oerations: "+model.getOperations());
            }

        }
        catch(Exception e){
            BRParsingException parsingException = new BRParsingException(e);
            parsingException.setAnalyzedText(dataSetStr);
            throw parsingException;
        }
    }

    private long nbOfDroitsDeGarde(List<Operation> operations){
        return operations.stream().filter(operation -> operation instanceof DroitsDeGarde).count();
    }

}
