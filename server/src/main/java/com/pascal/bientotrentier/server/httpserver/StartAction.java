package com.pascal.bientotrentier.server.httpserver;

import com.pascal.bientotrentier.service.config.MainSettings;
import com.pascal.bientotrentier.service.exporter.BRModelChecker;
import com.pascal.bientotrentier.service.exporter.BRModelExporter;
import com.pascal.bientotrentier.service.exporter.EZPortfolioManager;
import com.pascal.bientotrentier.service.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.service.model.BRModel;
import com.pascal.bientotrentier.service.sources.Reporting;
import com.pascal.bientotrentier.service.sources.bourseDirect.BourseDirectProcessor;
import com.pascal.bientotrentier.service.util.FileLinkCreator;
import com.pascal.bientotrentier.service.util.HtmlReporting;
import com.pascal.bientotrentier.service.util.MultiWriter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

public class StartAction {
    private final MainSettings mainSettings;

    public static final String REPORT_FILE_PREFIX =  "bientotRentier-report-";
    public static final String REPORT_FILE_SUFFIX =  ".html";

    StartAction(MainSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public void start(Writer htmlPageWriter, FileLinkCreator fileLinkCreator, boolean readOnly) throws IOException {
        File logsDir = new File(mainSettings.getBientotRentier().getLogsDir());
        logsDir.mkdirs();
        Date now = new Date();
        String reportFileName =  REPORT_FILE_PREFIX + new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(now) + REPORT_FILE_SUFFIX;
        File reportFile = new File(logsDir + File.separator + reportFileName);

        try (
                Writer fileWriter = new BufferedWriter(new FileWriter(reportFile));
                HtmlReporting reporting = new HtmlReporting(fileLinkCreator, new MultiWriter(fileWriter, htmlPageWriter))  // will write into the report & html Page
        ) {
            fileWriter.write("<html><head><meta charset='UTF-8'>\n");
            reporting.writeHeader(reporting.escape("Bientot Rentier Report")
                    + (readOnly ? "<br>(Simulation)" : "")
                    + "<br>"
                    + reporting.escape(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(now)));
            fileWriter.write("</head><body>\n");


            try {
                EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings.getEzPortfolio());
                EZPortfolio ezPortfolio = ezPortfolioManager.load();

                List<BRModel> allBRModels;
                try (Reporting ignored = reporting.pushSection("Launch BourseDirect Process")) {
                    allBRModels = new BourseDirectProcessor(mainSettings).start(reporting, ezPortfolio);
                }

                boolean isValid;
                try (Reporting ignored = reporting.pushSection("Checking Operations")) {
                    isValid = new BRModelChecker(reporting).generateReport(allBRModels);
                }

                if (isValid) {
                    new BRModelExporter(reporting).exportModels(allBRModels, ezPortfolio);
                    if (!readOnly) {
                        try (Reporting rep = reporting.pushSection("Updating EZPortfolio")) {
                            ezPortfolioManager.save(ezPortfolio);
                        }
                    }
                }
            }
            catch(Throwable t){
                reporting.error(t);
            }

            fileWriter.write("</body></html>\n");
            fileWriter.flush();
        }
    }

    public static Predicate<File> fileFilter(){
        return file -> file.getName().startsWith(StartAction.REPORT_FILE_PREFIX) && file.getName().endsWith(StartAction.REPORT_FILE_SUFFIX);
    }

    public static Predicate<File> dirFilter(){
        return file -> false; // there is no subdir for log
    }

}
