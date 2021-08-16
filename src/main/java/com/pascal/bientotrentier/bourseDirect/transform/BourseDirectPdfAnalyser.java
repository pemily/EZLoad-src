package com.pascal.bientotrentier.bourseDirect.transform;

import com.pascal.bientotrentier.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.bientotrentier.parsers.bourseDirect.BourseDirectPdfParser;
import com.pascal.bientotrentier.parsers.bourseDirect.Dataset;
import com.pascal.bientotrentier.parsers.bourseDirect.ParseException;
import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.util.BRException;
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
                        logger.info("Decoding file: "+ p.toFile().getAbsolutePath()+"...");
                        FileInputStream input = new FileInputStream(p.toFile());

                        PDDocument document = PDDocument.load(input);

                        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                        stripper.setSortByPosition(true);

                        PDFTextStripper tStripper = new PDFTextStripper();

                        String pdfText = tStripper.getText(document);

                        // generateFileForTest(pdfText);

                        analyzePdfText(pdfText);

                    } catch (IOException | BRException e) {
                        logger.error("Error while reading file: "+ p.toFile().getAbsolutePath(), e);
                        throw new RuntimeException(e);
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

    /*
Avis d'Opération
COMPTE N° 508TI00085554410EUR Ordinaire
MR EMILY PASCAL
Nous vous prions de trouver ci-dessous votre relevé d'opérations. Sans observation de votre part au
sujet du présent relevé, nous le considérerons comme ayant obtenu votre accord. Veuillez agréer nos
salutations distinguées.
Le 19/03/2021
MR EMILY PASCAL
192 ROUTE DE PEGOMAS
06130  GRASSE
Date Désignation Débit (€) Crédit (€)
 19/03/2021
 19/03/2021
 19/03/2021
 19/03/2021
 ACHAT COMPTANT  FR0013269123  RUBIS
 QUANTITE :  +120
 COURS :  +39,9  BRUT :  +4 788,00
 COURTAGE :  +4,31  TVA :  +0,00
 Heure Execution: 09:01:06       Lieu: BORSE BERLIN EQUIDUCT TRADING - BERL
 ACHAT ETRANGER  US03027X1000  AMERICAN TOWER
 QUANTITE :  +19
 COURS :  +187,230039236  BRUT :  +3 557,37
 COURTAGE :  +8,50  TVA :  +0,00
 COURS EN USD :  +222,85  TX USD/EUR :  +1,190247040
 Heure Execution: 14:30:03       Lieu: NEW YORK STOCK EXCHANGE, INC.
 ACHAT ETRANGER  US92936U1097  W.P. CAREY
 QUANTITE :  +60
 COURS :  +58,223207176  BRUT :  +3 493,39
 COURTAGE :  +8,50  TVA :  +0,00
 COURS EN USD :  +69,3  TX USD/EUR :  +1,190247040
 Heure Execution: 14:30:02       Lieu: NEW YORK STOCK EXCHANGE, INC.
 TAXE TRANSACT FINANCIERES  FR0013269123  TTF
 4 792,31
 3 565,87
 3 501,89
 14,36
Sous réserve de bonne fin / Ce relevé ne constitue pas une facture
Les montants des colonnes Débit et Crédit sont stipulés TVA Comprise
1/1
Bourse Direct ,  SA au capital de 13.988.845,75 €, R.C.S Paris B 408 790 608, Siège Social : 374 rue Saint-Honoré, 75001 Paris -  Groupe VIEL et Cie
     */

    public BourseDirectModel analyzePdfText(String text) throws BRException {
        try {
            text = StringUtils.clean(text);
            BourseDirectModel model = new BourseDirectModel();
            analyzePdfText(text, model);
            return model;
        }
        catch(Exception e){
            throw new BRException("Error when analyzing text:\n##################################################\n"+text+"\n################################################\n", e);
        }
    }


    private void analyzePdfText(String text, BourseDirectModel model) {

        String section[] = text.split(TEXT_INTRO);
        String accountSection[] = section[0].split("[\\n]");
        model.setAccountOwnerName(accountSection[2].trim());
       /* {
           String avisOpereAndAccount = accountSection[0]+"\r"+accountSection[1];
            BourseDirectParser parser = new BourseDirectParser(new StringReader(avisOpereAndAccount));
            Account account = parser.Account();
            String id = account.getId();
            String type = account.getType();
        }*/

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
            parser.setTracingEnabled(true);
            Dataset dataset = parser.Dataset();
            model.setOperations(dataset.getOperations());
            model.setDates(dataset.getDates());
            model.setAmounts(dataset.getAmounts());

            // little check
            if (model.getDates().size() != model.getOperations().size()){
                throw new BRException("The number of dates found: "+model.getDates().size()+" do not match the number of operations found: "+model.getOperations().size());
            }
            if (model.getAmounts().size() != model.getOperations().size()){
                throw new BRException("The number of amounts found: "+model.getDates().size()+" do not match the number of operations found: "+model.getOperations().size());
            }

        }
        catch(Exception e){
            throw new BRException("Input Text:\n=======================================================\n"+dataSetStr+"\n=======================================================", e);
        }
    }

}
