package com.pascal.bientotrentier;

import com.google.gson.Gson;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.util.LoggerReporting;
import com.pascal.bientotrentier.sources.bourseDirect.transform.BourseDirectProcessor;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String args[]) throws IOException {
        MainSettings mainSettings = loadProps();

        LoggerReporting reporting = new LoggerReporting();
        // new BourseDirectExtractor(mainSettings).start(reporting);

        List<BRModel> allBRModels = new BourseDirectProcessor(mainSettings).start(reporting);
    }

    public static MainSettings loadProps() throws FileNotFoundException {
        return new Gson().fromJson(new FileReader("src/main/resources/bientotRentier.json"), MainSettings.class);
    }



}
