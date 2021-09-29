package com.pascal.ezload.server.httpserver.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.BRModelChecker;
import com.pascal.ezload.service.exporter.BRModelExporter;
import com.pascal.ezload.service.exporter.EZPortfolioManager;
import com.pascal.ezload.service.exporter.ezPortfolio.EZPortfolio;
import com.pascal.ezload.service.model.BRModel;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectProcessor;
import com.pascal.ezload.service.util.FileLinkCreator;
import com.pascal.ezload.service.util.HtmlReporting;
import com.pascal.ezload.service.util.MultiWriter;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

@Path("engine")
public class EngineHandler {

    public static final String REPORT_FILE_PREFIX =  "bientotRentier-report-";
    public static final String REPORT_FILE_SUFFIX =  ".html";

    @Context
    private HttpServletResponse response;

    @Inject
    private EZHttpServer httpServer;

    @PUT
    @Path("/start")
    public void start(@NotNull @QueryParam("simulation") boolean simulation,
                      @NotNull @QueryParam("chromeVersion") String chromeVersion) throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        start(chromeVersion, mainSettings,
                response.getWriter(), httpServer.fileLinkCreator(mainSettings), simulation);
    }

    private void start(String currentChromeVersion, MainSettings mainSettings, Writer htmlPageWriter, FileLinkCreator fileLinkCreator, boolean readOnly) throws IOException {
        File logsDir = new File(mainSettings.getEZLoad().getLogsDir());
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
                    allBRModels = new BourseDirectProcessor(mainSettings).start(reporting, currentChromeVersion, SettingsManager.saveNewChromeDriver(), ezPortfolio);
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

}
