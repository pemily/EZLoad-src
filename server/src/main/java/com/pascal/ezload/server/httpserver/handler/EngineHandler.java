package com.pascal.ezload.server.httpserver.handler;

import java.util.List;
import java.util.stream.Collectors;

import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.EZModelChecker;
import com.pascal.ezload.service.exporter.EzEditionExporter;
import com.pascal.ezload.service.exporter.EZPortfolioManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.model.EnumEZCourtier;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectAnalyser;

import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;
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

    @Inject
    private EzServerState serverState;

    @GET
    @Path("/downloadAndAnalyse")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess download(@NotNull @QueryParam("chromeVersion") String chromeVersion,
                      @NotNull @QueryParam("courtier") EnumEZCourtier courtier) throws Exception {
        if (courtier != EnumEZCourtier.BourseDirect) {
            throw new IllegalArgumentException("Cette operation n'est pas encore développé pour le courtier: "+courtier.getEzPortfolioName());
        }
        else {
            MainSettings mainSettings = SettingsManager.getInstance().loadProps();
            return processManager.createNewRunningProcess(mainSettings,
                    "Téléchargement des nouvelles opérations de " + courtier.getEzPortfolioName() + " et Analyse",
                    ProcessManager.getLog(mainSettings, courtier.getDirName(), "-downloadAndAnalyze.log"),
                    (processLogger) -> {
                        Reporting reporting = processLogger.getReporting();

                        EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings);
                        EZPortfolioProxy ezPortfolioProxy = ezPortfolioManager.load();

                        BourseDirectDownloader bourseDirectDownloader = new BourseDirectDownloader(reporting, mainSettings);
                        // Donwload the files, according to the last date retrieved from ezPortfolio
                        bourseDirectDownloader.start(chromeVersion, SettingsManager.saveNewChromeDriver(), ezPortfolioProxy);
                    });
        }
    }

    @GET
    @Path("/analyze")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess analyze() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        EnumEZCourtier courtier = EnumEZCourtier.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings,
                "Analyse des nouvelles opérations de " + courtier.getEzPortfolioName(),
                ProcessManager.getLog(mainSettings, courtier.getDirName(), "-downloadAndAnalyze.log"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();

                    EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings);
                    EZPortfolioProxy ezPortfolioProxy = ezPortfolioManager.load();

                    List<EZModel> allEZModels;
                    try (Reporting ignored = reporting.pushSection("BourseDirect Analyse")) {
                        allEZModels = new BourseDirectAnalyser(mainSettings).start(reporting, ezPortfolioProxy);
                    }

                    try (Reporting ignored = reporting.pushSection("Vérification des Opérations")) {
                        new EZModelChecker(reporting).validateModels(allEZModels);
                    }

                    List<EzReport> allEzReports = new EzEditionExporter(mainSettings, reporting).exportModels(allEZModels, ezPortfolioProxy);
                    serverState.setEzReports(allEzReports);
                });

    }


    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    // upload valid operations into GoogleDriver EZPortfolio
    public EzProcess upload() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        EnumEZCourtier courtier = EnumEZCourtier.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings,
                "Mise à jour d'EZPortfolio avec les opérations validé",
                ProcessManager.getLog(mainSettings, courtier.getDirName(), "-downloadAndAnalyze.log"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
            try (Reporting rep = reporting.pushSection("Updating EZPortfolio")) {
                EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings);
                EZPortfolioProxy ezPortfolioProxy = ezPortfolioManager.load();
                serverState.setEzReports(ezPortfolioProxy.save(serverState.getEzReports()));
            }
        });
    }

}
