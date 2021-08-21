package com.pascal.bientotrentier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.bientotrentier.exporter.BRModelChecker;
import com.pascal.bientotrentier.exporter.BRModelExporter;
import com.pascal.bientotrentier.exporter.EZPortfolioHandler;
import com.pascal.bientotrentier.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.transform.BourseDirectProcessor;
import com.pascal.bientotrentier.util.HtmlReporting;
import com.pascal.bientotrentier.util.MiniHttpServer;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class Main {

    public static void main(String args[]) throws Exception {
        MainSettings mainSettings = loadProps();

        File logsDir = new File(mainSettings.getBientotRentier().getLogsDir());
        logsDir.mkdirs();
        File reportFile = new File(logsDir+File.separator+"bientotRentier-report-"+new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date())+".html");

        try(Writer writer = new BufferedWriter(new FileWriter(reportFile))){
            HtmlReporting reporting = new HtmlReporting("Bientot Rentier Report", writer);
            try(MiniHttpServer server = new MiniHttpServer()) {
                server.start(reportFile, HtmlReporting.END_DOC);
                Desktop.getDesktop().browse(new URI("http://localhost:8000/bientotRentier"));

                try(Reporting top = reporting.pushSection("Bientot Rentier Report - "+reportFile.getAbsolutePath())) {

                    try (Reporting rep = reporting.pushSection("Extract data from BourseDirect")) {
                        // new BourseDirectExtractor(mainSettings).start(reporting);
                    }

                    List<BRModel> allBRModels = null;
                    try (Reporting rep = reporting.pushSection("Analyze BourseDirect PDF file")) {
                        allBRModels = new BourseDirectProcessor(mainSettings).start(reporting);
                    }

                    boolean isValid = false;
                    try (Reporting rep = reporting.pushSection("Checking Operations")) {
                        isValid = new BRModelChecker(reporting).isActionValid(allBRModels);
                    }

                    if (isValid) {
                        try (Reporting rep = reporting.pushSection("Updating EZPortfolio")) {
                            EZPortfolioHandler ezPortfolioHandler = new EZPortfolioHandler(reporting, mainSettings.getEzPortfolio());
                            EZPortfolio ezPortfolio = ezPortfolioHandler.load();
                            new BRModelExporter(reporting).exportModels(allBRModels, ezPortfolio);
                            ezPortfolioHandler.save(ezPortfolio);
                        }
                    }
                }
                reporting.end();
            }
        }
        Desktop.getDesktop().open(reportFile);
    }

    private static MainSettings loadProps() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MainSettings settings = mapper.readValue(new FileReader("src/main/resources/bientotRentier.yaml"), MainSettings.class);
        return settings;
    }



}
