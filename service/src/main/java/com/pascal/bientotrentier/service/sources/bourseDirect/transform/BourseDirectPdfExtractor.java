package com.pascal.bientotrentier.service.sources.bourseDirect.transform;

import com.pascal.bientotrentier.service.sources.Reporting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BourseDirectPdfExtractor {

    private final Reporting reporting;

    public BourseDirectPdfExtractor(Reporting reporting){
        this.reporting = reporting;
    }

    public String getText(String pdfFilePath) throws IOException {
        reporting.info("Reading pdf...");
        InputStream input = new BufferedInputStream(new FileInputStream(pdfFilePath));

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
