package com.pascal.bientotrentier.sources.bourseDirect.transform;

import com.pascal.bientotrentier.sources.Reporting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.FileInputStream;
import java.io.IOException;

public class BourseDirectPdfExtractor {

    private Reporting reporting;

    public BourseDirectPdfExtractor(Reporting reporting){
        this.reporting = reporting;
    }

    public String getText(String pdfFilePath) throws IOException {
        reporting.info("Reading pdf...");
        FileInputStream input = new FileInputStream(pdfFilePath);

        PDDocument document = PDDocument.load(input);

        PDFTextStripper stripper = new PDFTextStripper();
        /*
        stripper.setParagraphStart("@@PStart@@");
        stripper.setParagraphEnd("@@PEnd@@");
        stripper.setWordSeparator("<@@>");
        */

        String pdfText = stripper.getText(document);
        document.close();
        reporting.info("pdf read => ok");
        return pdfText;
    }
}