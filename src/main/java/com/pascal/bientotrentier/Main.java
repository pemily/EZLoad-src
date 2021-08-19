package com.pascal.bientotrentier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.bientotrentier.exporter.BRModelChecker;
import com.pascal.bientotrentier.exporter.BRModelExporter;
import com.pascal.bientotrentier.exporter.EZPortfolioHandler;
import com.pascal.bientotrentier.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.sources.bourseDirect.transform.BourseDirectProcessor;
import com.pascal.bientotrentier.util.LoggerReporting;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class Main {

    public static void main(String args[]) throws Exception {
        MainSettings mainSettings = loadProps();

        LoggerReporting reporting = new LoggerReporting();
        // new BourseDirectExtractor(mainSettings).start(reporting);

        List<BRModel> allBRModels = new BourseDirectProcessor(mainSettings).start(reporting);

        boolean isValid = new BRModelChecker(reporting).isActionValid(allBRModels);
        if (isValid) {
            EZPortfolioHandler ezPortfolioHandler = new EZPortfolioHandler(reporting, mainSettings.getEzPortfolio());
            EZPortfolio ezPortfolio = ezPortfolioHandler.load();
            new BRModelExporter(reporting).exportModels(allBRModels, ezPortfolio);
            ezPortfolioHandler.save(ezPortfolio);
        }
    }

    private static MainSettings loadProps() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MainSettings settings = mapper.readValue(new FileReader("src/main/resources/bientotRentier.yaml"), MainSettings.class);
        return settings;
    }



}
