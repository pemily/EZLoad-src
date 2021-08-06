package com.pascal.bientotrentier;

import com.google.gson.Gson;
import com.pascal.bientotrentier.bourseDirect.transform.BourseDirectPdfAnalyser;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String args[]) throws IOException {
        MainSettings mainSettings = loadProps();

        // new BourseDirectExtractor(mainSettings).start();

        new BourseDirectPdfAnalyser(mainSettings).start();
    }

    public static MainSettings loadProps() throws FileNotFoundException {
        return new Gson().fromJson(new FileReader("src/main/resources/bientotRentier.json"), MainSettings.class);
    }



}
