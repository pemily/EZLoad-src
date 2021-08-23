package com.pascal.bientotrentier.server;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.exporter.BRModelChecker;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.transform.BourseDirectProcessor;
import com.pascal.bientotrentier.util.HtmlReporting;
import com.pascal.bientotrentier.util.MultiWriter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StartAction {
    private final MainSettings mainSettings;

    public static final String REPORT_FILE_PREFIX =  "bientotRentier-report-";
    public static final String REPORT_FILE_SUFFIX =  ".html";

    StartAction(MainSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public void start(Writer htmlPageWriter) throws IOException {
        File logsDir = new File(mainSettings.getBientotRentier().getLogsDir());
        logsDir.mkdirs();
        File reportFile = new File(logsDir + File.separator + REPORT_FILE_PREFIX + new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date()) + REPORT_FILE_SUFFIX);

        try (Writer reportWriter = new BufferedWriter(new FileWriter(reportFile))) {
            reportWriter.write("<html><head><meta charset='UTF-8'>\n");
            try (HtmlReporting reporting = new HtmlReporting("Bientot Rentier Report", new MultiWriter(reportWriter, htmlPageWriter))) {
                reportWriter.write("</head><body>\n");

                try (Reporting top = reporting.pushSection("Bientot Rentier Report - " + reportFile.getAbsolutePath())) {

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
                     /*       EZPortfolioHandler ezPortfolioHandler = new EZPortfolioHandler(reporting, mainSettings.getEzPortfolio());
                            EZPortfolio ezPortfolio = ezPortfolioHandler.load();
                            new BRModelExporter(reporting).exportModels(allBRModels, ezPortfolio);
                            ezPortfolioHandler.save(ezPortfolio);*/
                        }
                    }
                }
                reportWriter.write("</body></html>\n");
                reportWriter.flush();
            }
        }
    }

}
