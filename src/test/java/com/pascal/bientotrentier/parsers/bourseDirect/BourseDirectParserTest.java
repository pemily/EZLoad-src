package com.pascal.bientotrentier.parsers.bourseDirect;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.bourseDirect.transform.BourseDirectPdfAnalyser;
import com.pascal.bientotrentier.bourseDirect.transform.BourseDirectVisitorModel;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BourseDirectParserTest {

    private String readFile(String file) {
        String text = new BufferedReader(new InputStreamReader(BourseDirectParserTest.class.getResourceAsStream(file)))
                .lines().collect(Collectors.joining("\n"));
        System.out.println("###################################################################");
        System.out.println(text);
        System.out.println("###################################################################");
        return text;
    }

    @Test
    public void testAvisOperation1() throws ParseException {
        String text = readFile("avisOperation1.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("24/02/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation2() throws ParseException {
        String text = readFile("avisOperation2.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("26/02/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation3() throws ParseException {
        String text = readFile("avisOperation3.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("01/03/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }

    @Test
    public void testAvisOperation4() throws ParseException {
        String text = readFile("avisOperation4.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("02/03/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }

    @Test
    public void testAvisOperation5() throws ParseException {
        String text = readFile("avisOperation5.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("03/03/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }

    @Test
    public void testAvisOperation6() throws ParseException {
        String text = readFile("avisOperation6.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("08/03/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation7() throws ParseException {
        String text = readFile("avisOperation7.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("09/03/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }

    @Test
    public void testAvisOperation8() throws ParseException {
        String text = readFile("avisOperation8.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("16/03/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation9() throws ParseException {
        String text = readFile("avisOperation9.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("18/03/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }

    @Test
    public void testAvisOperation10() throws ParseException {
        String text = readFile("avisOperation10.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("19/03/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }

    @Test
    public void testAvisOperation11() throws ParseException {
        String text = readFile("avisOperation11.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("30/03/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation12() throws ParseException {
        String text = readFile("avisOperation12.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("07/04/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation13() throws ParseException {
        String text = readFile("avisOperation13.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("19/04/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }

    @Test
    public void testAvisOperation14() throws ParseException {
        String text = readFile("avisOperation14.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("30/04/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation15() throws ParseException {
        String text = readFile("avisOperation15.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("11/05/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }



    @Test
    public void testAvisOperation16() throws ParseException {
        String text = readFile("avisOperation16.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("18/05/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation17() throws ParseException {
        String text = readFile("avisOperation17.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("06/07/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation18() throws ParseException {
        String text = readFile("avisOperation18.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("08/07/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }



    @Test
    public void testAvisOperation19() throws ParseException {
        String text = readFile("avisOperation19.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("12/07/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }



    @Test
    public void testAvisOperation20() throws ParseException {
        String text = readFile("avisOperation20.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("15/07/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }


    @Test
    public void testAvisOperation21() throws ParseException {
        String text = readFile("avisOperation21.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("19/07/2021", model.getDateAvisOperation());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }
}
