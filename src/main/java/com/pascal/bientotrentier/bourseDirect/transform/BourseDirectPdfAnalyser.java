package com.pascal.bientotrentier.bourseDirect.transform;

import com.pascal.bientotrentier.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.parsers.bourseDirect.BourseDirectParser;
import com.pascal.bientotrentier.parsers.bourseDirect.ParseException;
import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.util.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
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
                        logger.info("Decoding file: "+ p.toFile().getAbsolutePath()+"...");
                        FileInputStream input = new FileInputStream(p.toFile());

                        PDDocument document = PDDocument.load(input);

                        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                        stripper.setSortByPosition(true);

                        PDFTextStripper tStripper = new PDFTextStripper();

                        String pdfText = tStripper.getText(document);

                        analyzePdfText(pdfText);



                    } catch (IOException | ParseException e) {
                        logger.error("Error while reading file: "+ p.toFile().getAbsolutePath(), e);
                        throw new RuntimeException(e);
                    }
                });
        }
    }

    /*
Avis d'Opération
COMPTE N° 508TI00085554410EUR Ordinaire
MR EMILY PASCAL
Nous vous prions de trouver ci-dessous votre relevé d'opérations. Sans observation de votre part au
sujet du présent relevé, nous le considérerons comme ayant obtenu votre accord. Veuillez agréer nos
salutations distinguées.
Le 24/02/2021
MR EMILY PASCAL
192 ROUTE DE PEGOMAS
06130  GRASSE
Date Désignation Débit (€) Crédit (€)
 24/02/2021  VIREMENT ESPECES  VIRT M. ET/OU MME  2 400,00
Sous réserve de bonne fin / Ce relevé ne constitue pas une facture
Les montants des colonnes Débit et Crédit sont stipulés TVA Comprise
1/1
Bourse Direct ,  SA au capital de 13.988.845,75 €, R.C.S Paris B 408 790 608, Siège Social : 374 rue Saint-Honoré, 75001 Paris -  Groupe VIEL et Cie
     */
    public BourseDirectVisitorModel analyzePdfText(String text) throws ParseException {
        BourseDirectVisitorModel model = new BourseDirectVisitorModel();

        String section[] = text.split(TEXT_INTRO);
        String accountSection[] = section[0].split("[\\n]");
        String avisOpereAndAccount = accountSection[0]+"\r"+accountSection[1];
        model.setAccountOwnerName(accountSection[2].trim());

        BourseDirectParser bourseDirectAccountParser = new BourseDirectParser(new StringReader(avisOpereAndAccount));
        bourseDirectAccountParser.account().jjtAccept(new BD_PdfExtractDataVisitor(), model);

        String textDebutTableau = DATE_DÉSIGNATION_DÉBIT;
        String section2[] = section[1].split(Pattern.quote(textDebutTableau));
        String section2Splitted[] = section2[0].split("[\\n]");
        String leDate = section2Splitted[0]; // Le 24/02/2021
        if (!leDate.startsWith("Le ")){
            throw new ParseException("Invalid Format detected.");
        }
        model.setDateAvisOpere(leDate.substring(3).trim());

        List<String> addr = new LinkedList<>(Arrays.asList(section2Splitted));
        addr.remove(0);
        model.setAddress(addr.stream().collect(Collectors.joining("\n")).trim());

        String deviseResearch[] = section2[1].split("[\\(\\)]");
        model.setDeviseDebit(deviseResearch[0].trim());
        model.setDeviseCredit(deviseResearch[2].trim());

        String prepareDataSet = StringUtils.divide(section2[1], model.getDeviseDebit()+") Crédit ("+model.getDeviseCredit()+")")[1];
        String dataSet = StringUtils.divide(prepareDataSet, FOOTER)[0].trim();

        BourseDirectParser bourseDirectDataSetParser = new BourseDirectParser(new StringReader(dataSet));
        bourseDirectDataSetParser.dataset().jjtAccept(new BD_PdfExtractDataVisitor(), model);

        return model;
    }
}
