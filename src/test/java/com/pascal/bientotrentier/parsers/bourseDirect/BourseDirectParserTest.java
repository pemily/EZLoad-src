package com.pascal.bientotrentier.parsers.bourseDirect;

import com.pascal.bientotrentier.bourseDirect.transform.BourseDirectVisitorModel;
import com.pascal.bientotrentier.bourseDirect.transform.BD_PdfExtractDataVisitor;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BourseDirectParserTest {

    private String text = "Avis d'Opération\n" +
            "COMPTE N° 508TI00085554410EUR Ordinaire\n" ; /*+
            "MR EMILY PASCAL\n" +
            "Nous vous prions de trouver ci-dessous votre relevé d'opérations. Sans observation de votre part au\n" +
            "sujet du présent relevé, nous le considérerons comme ayant obtenu votre accord. Veuillez agréer nos\n" +
            "salutations distinguées.\n" +
            "Le 24/02/2021\n" +
            "MR EMILY PASCAL\n" +
            "192 ROUTE DE PEGOMAS\n" +
            "06130  GRASSE\n" +
            "Date Désignation Débit (€) Crédit (€)\n" +
            " 24/02/2021  VIREMENT ESPECES  VIRT M. ET/OU MME  2 400,00\n" +
            "Sous réserve de bonne fin / Ce relevé ne constitue pas une facture\n" +
            "Les montants des colonnes Débit et Crédit sont stipulés TVA Comprise\n" +
            "1/1\n" +
            "Bourse Direct ,  SA au capital de 13.988.845,75 €, R.C.S Paris B 408 790 608, Siège Social : 374 rue Saint-Honoré, 75001 Paris -  Groupe VIEL et Cie\n";
*/
    @Test
    public void testAvisOperation1() throws ParseException {
        //Reader reader = new BufferedReader(new InputStreamReader(BourseDirectParserTest.class.getResourceAsStream("avisOperation1.txt")));
        Reader reader = new StringReader(text);
        BourseDirectParser bourseDirectParser = new BourseDirectParser(reader);
        bourseDirectParser.enable_tracing();
        SimpleNode root = bourseDirectParser.start();

        BD_PdfExtractDataVisitor visitor = new BD_PdfExtractDataVisitor();
        BourseDirectVisitorModel model = new BourseDirectVisitorModel();
        root.jjtAccept(visitor, model);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());

    }
}
