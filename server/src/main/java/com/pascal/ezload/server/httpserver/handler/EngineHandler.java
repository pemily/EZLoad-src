package com.pascal.ezload.server.httpserver.handler;

import java.util.List;

import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.BRModelChecker;
import com.pascal.ezload.service.exporter.BRModelExporter;
import com.pascal.ezload.service.exporter.EZPortfolioManager;
import com.pascal.ezload.service.exporter.ezPortfolio.EZPortfolio;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.model.EnumEZCourtier;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectProcessor;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("engine")
public class EngineHandler {

    public static final String REPORT_FILE_PREFIX =  "bientotRentier-report-";
    public static final String REPORT_FILE_SUFFIX =  ".html";

    @Inject
    private ProcessManager processManager;


    @GET
    @Path("/downloadAndAnalyse")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadAndAnalyze(@NotNull @QueryParam("chromeVersion") String chromeVersion,
                      @NotNull @QueryParam("courtier") EnumEZCourtier courtier) throws Exception {
        if (courtier != EnumEZCourtier.BourseDirect) {
            throw new IllegalArgumentException("Cette operation n'est pas encore développé pour le courtier: "+courtier.getEzPortfolioName());
        }
        else {
            bourseDirectDownload(chromeVersion);
        }
    }

    private EzProcess bourseDirectDownload(String chromeVersion) throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        EnumEZCourtier courtier = EnumEZCourtier.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings,
                "Téléchargement des nouvelles opérations de " + courtier.getEzPortfolioName()+" et Analyse",
                ProcessManager.getLog(mainSettings, courtier.getDirName(), "-downloadAndAnalyze.log"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();

                    EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings.getEzPortfolio());
                    EZPortfolio ezPortfolio = ezPortfolioManager.load();

                    List<EZModel> allEZModels;
                    try (Reporting ignored = reporting.pushSection("Launch BourseDirect Process")) {
                        allEZModels = new BourseDirectProcessor(mainSettings).start(reporting, chromeVersion, SettingsManager.saveNewChromeDriver(), ezPortfolio);
                    }

                    try (Reporting ignored = reporting.pushSection("Checking Operations")) {
                        new BRModelChecker(reporting).generateReport(allEZModels);
                    }

                    new BRModelExporter(reporting).exportModels(allEZModels, ezPortfolio);
                });
    }


    private EzProcess uploadToEzPortfolio() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        EnumEZCourtier courtier = EnumEZCourtier.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings,
                "Mise à jour d'EZPortfolio avec les opérations validé",
                ProcessManager.getLog(mainSettings, courtier.getDirName(), "-downloadAndAnalyze.log"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
            try (Reporting rep = reporting.pushSection("Updating EZPortfolio")) {
                EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings.getEzPortfolio());
                EZPortfolio ezPortfolio = ezPortfolioManager.load();
                List<EZModel> allEZModels = null;
                new BRModelExporter(reporting).exportModels(allEZModels, ezPortfolio);

                ezPortfolioManager.save(ezPortfolio);
            }
        });
    }

}
