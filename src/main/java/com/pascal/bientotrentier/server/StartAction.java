package com.pascal.bientotrentier.server;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.exporter.BRModelChecker;
import com.pascal.bientotrentier.exporter.BRModelExporter;
import com.pascal.bientotrentier.exporter.EZPortfolioManager;
import com.pascal.bientotrentier.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectProcessor;
import com.pascal.bientotrentier.util.FileLinkCreator;
import com.pascal.bientotrentier.util.HtmlReporting;
import com.pascal.bientotrentier.util.MultiWriter;

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

    public void start(Writer htmlPageWriter, FileLinkCreator fileLinkCreator) throws IOException {
        File logsDir = new File(mainSettings.getBientotRentier().getLogsDir());
        logsDir.mkdirs();
        Date now = new Date();
        String reportFileName =  REPORT_FILE_PREFIX + new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(now) + REPORT_FILE_SUFFIX;
        File reportFile = new File(logsDir + File.separator + reportFileName);

        try (
                Writer fileWriter = new BufferedWriter(new FileWriter(reportFile));
                HtmlReporting reporting =
                        new HtmlReporting("Bientot Rentier Report - " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(now), fileLinkCreator, new MultiWriter(fileWriter, htmlPageWriter))
        ) {
            fileWriter.write("<html><head><meta charset='UTF-8'>\n");
            reporting.writeHeader(); // will write into the report & html Page
            fileWriter.write("</head><body>\n");


            try {
                EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings.getEzPortfolio());
                EZPortfolio ezPortfolio = ezPortfolioManager.load();

                List<BRModel> allBRModels;
                try (Reporting rep = reporting.pushSection("Launch BourseDirect Process")) {
                    allBRModels = new BourseDirectProcessor(mainSettings).start(reporting, ezPortfolio);
                }

                boolean isValid;
                try (Reporting rep = reporting.pushSection("Checking Operations")) {
                    isValid = new BRModelChecker(reporting).isActionValid(allBRModels);
                }

                if (isValid) {
                    try (Reporting rep = reporting.pushSection("Updating EZPortfolio")) {
                        new BRModelExporter(reporting).exportModels(allBRModels, ezPortfolio);
                        ezPortfolioManager.save(ezPortfolio);
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
