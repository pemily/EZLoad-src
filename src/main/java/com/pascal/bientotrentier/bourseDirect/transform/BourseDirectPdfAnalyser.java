package com.pascal.bientotrentier.bourseDirect.transform;

import com.pascal.bientotrentier.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.parsers.bourseDirect.BourseDirectParser;
import com.pascal.bientotrentier.parsers.bourseDirect.ParseException;
import com.pascal.bientotrentier.MainSettings;
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
import java.util.stream.Stream;

public class BourseDirectPdfAnalyser {

    private static final Logger logger = Logger.getLogger(BourseDirectPdfAnalyser.class);

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

                        BourseDirectParser bourseDirectParser = new BourseDirectParser(new StringReader("20/01/2021"));
                        bourseDirectParser.enable_tracing();
                        bourseDirectParser.start();



                    } catch (IOException | ParseException e) {
                        logger.error("Error while reading file: "+ p.toFile().getAbsolutePath(), e);
                        throw new RuntimeException(e);
                    }
                });
        }
    }


    public void analyzePdfText(String text) throws ParseException {
        BourseDirectParser bourseDirectParser = new BourseDirectParser(new StringReader(text));
        bourseDirectParser.enable_tracing();
        bourseDirectParser.start();

    }
}
