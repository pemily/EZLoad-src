package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.service.sources.Reporting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class BourseDirectPdfExtractor {

    private final Reporting reporting;
    private final String pdfFilePath;
    LinkedList<PositionText> allTexts = new LinkedList<>();

    public BourseDirectPdfExtractor(Reporting reporting, String pdfFilePath){
        this.reporting = reporting;
        this.pdfFilePath = pdfFilePath;
    }


    public Result process() throws IOException {
        reporting.info("Reading pdf...");
        InputStream input = new BufferedInputStream(new FileInputStream(pdfFilePath));

        PDDocument document = PDDocument.load(input);

        PDFTextStripper stripper = new PDFTextStripper(){
            @Override
            protected void processTextPosition(TextPosition text) {
                process(text.getX(), text.getY(), text.getUnicode());
                super.processTextPosition(text);
            }
        };

        String pdfText = stripper.getText(document);
        document.close();

        reporting.info("pdf read => ok");
        return new Result(pdfText, allTexts);
    }


    private void process(float x, float y, String s){
        if (allTexts.isEmpty()){
            allTexts.add(new PositionText(x, y, s));
        }
        else {
            PositionText last = allTexts.getLast();
            if (last.sameSentence(x, y)) {
                last.addNext(x, y, s);
            } else {
                allTexts.add(new PositionText(x, y, s));
            }
        }
    }

    public static class PositionText {
        private static final int DIFF_BETWEEN_2_CHAR = 7;// difference max sur l'axe des X entre 2 caracteres (c'est approximatif, car en fonction des lettres ca peut etre moins)
        private final StringBuilder sentence;
        private final float startX, startY;
        private float lastX, lastY;

        PositionText(float startX, float startY, String initText) {
            this.lastX = this.startX = startX;
            this.lastY = this.startY = startY;
            sentence = new StringBuilder(initText);
        }

        private void addNext(float x, float y, String s){
            this.sentence.append(s);
            this.lastX = x;
            this.lastY = y;
        }

        private boolean sameSentence(float testX, float testY){
            return lastY == testY && testX >= lastX && testX <= lastX + DIFF_BETWEEN_2_CHAR;
        }

        public float getX(){
            return startX;
        }

        public float getY(){
            return startY;
        }

        public String getText(){
            return sentence.toString().trim();
        }

        public boolean sameX(float x){
            return startX >= x-1 && startX <= x+1; // +1 -1 car les coordonnées sont approximatives
        }

        public boolean sameY(float y){
            return startY >= y-1 && startY <= y+1; // +1 -1 car les coordonnées sont approximatives
        }

        public boolean isAfter(float x){
            return lastX > x;
        }

        public boolean isBefore(float x){
            return startX < x;
        }

        public boolean isBelow(float y) {
            return startY > y;
        }

        public boolean isAbove(float y) {
            return startY < y;
        }
    }

    public static class Result {
        private final String pdfText;
        private final List<PositionText> allPositionTexts;

        public Result(String pdfText, List<PositionText> allTexts) {
            this.pdfText = pdfText;
            this.allPositionTexts = allTexts;
        }

        public List<PositionText> getAllPositionTexts(){
            return allPositionTexts;
        }

        public String getPdfText(){
            return pdfText;
        }
    }
}
