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

    @Test
    public void testAvisOperation1() throws ParseException {
        String text = readFile("avisOperation1.txt");
        BourseDirectVisitorModel model = new BourseDirectPdfAnalyser(new MainSettings()).analyzePdfText(text);

        assertEquals("508TI00085554410EUR", model.getAccountNumber());
        assertEquals("Ordinaire", model.getAccountType());
        assertEquals("MR EMILY PASCAL", model.getAccountOwnerName());
        assertEquals("24/02/2021", model.getDateAvisOpere());
        assertEquals( "MR EMILY PASCAL\n192 ROUTE DE PEGOMAS\n06130  GRASSE", model.getAddress());
        assertEquals("€", model.getDeviseCredit());
        assertEquals("€", model.getDeviseDebit());
    }

    private String readFile(String file) {
        return new BufferedReader(new InputStreamReader(BourseDirectParserTest.class.getResourceAsStream(file))).lines().collect(Collectors.joining("\n"));
    }
}
