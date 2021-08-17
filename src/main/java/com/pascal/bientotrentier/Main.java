package com.pascal.bientotrentier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.sources.bourseDirect.transform.BourseDirectProcessor;
import com.pascal.bientotrentier.util.LoggerReporting;
import org.apache.log4j.Logger;

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

    public static MainSettings loadProps() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MainSettings settings = mapper.readValue(new FileReader("src/main/resources/bientotRentier.yaml"), MainSettings.class);
        return settings;
    }



}
