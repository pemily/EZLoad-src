package com.pascal.ezload.server.httpserver.handler;

import java.util.*;
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
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.PRU;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectAnalyser;

import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;
import com.pascal.ezload.service.util.ShareUtil;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

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
                      @NotNull @QueryParam("courtier") EnumEZBroker courtier) throws Exception {
        if (courtier != EnumEZBroker.BourseDirect) {
            throw new IllegalArgumentException("Cette operation n'est pas encore développé pour le courtier: "+courtier.getEzPortfolioName());
        }
        else {
            MainSettings mainSettings = SettingsManager.getInstance().loadProps();
            return processManager.createNewRunningProcess(mainSettings,
                    "Téléchargement des nouvelles opérations de " + courtier.getEzPortfolioName() + " et Analyse",
                    ProcessManager.getLog(mainSettings, courtier.getDirName(), "-downloadAndAnalyze.html"),
                    (processLogger) -> {
                        Reporting reporting = processLogger.getReporting();

                        EZPortfolioProxy ezPortfolioProxy = loadEzPortfolioProxyOrGetFromCache(mainSettings, reporting);

                        BourseDirectDownloader bourseDirectDownloader = new BourseDirectDownloader(reporting, mainSettings);
                        // Donwload the files, according to the last date retrieved from ezPortfolio
                        bourseDirectDownloader.start(chromeVersion, SettingsManager.saveNewChromeDriver(), ezPortfolioProxy);


                        // get the new version, and update the list of file not yet loaded
                        updateNotYetLoaded(mainSettings, reporting, ezPortfolioProxy);
                    });
        }
    }

    private EZPortfolioProxy loadEzPortfolioProxyOrGetFromCache(MainSettings mainSettings, Reporting reporting) throws Exception {
        EZPortfolioProxy ezPortfolioProxy = serverState.getEzPortfolioProxy();
        if (ezPortfolioProxy == null) {
            EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings);
            ezPortfolioProxy = ezPortfolioManager.load();
            serverState.setEzPortfolioProxy(ezPortfolioProxy);
        }
        return ezPortfolioProxy.createDeepCopy();
    }

    @GET
    @Path("/analyze")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess analyze() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        EnumEZBroker courtier = EnumEZBroker.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings,
                "Analyse des nouvelles opérations de " + courtier.getEzPortfolioName(),
                ProcessManager.getLog(mainSettings, courtier.getDirName(), "-analyze.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();

                    EZPortfolioProxy ezPortfolioProxy = loadEzPortfolioProxyOrGetFromCache(mainSettings, reporting);
                    Set<ShareValue> knownValues = ezPortfolioProxy.getShareValues();

                    List<EZModel> allEZModels;
                    try (Reporting ignored = reporting.pushSection("BourseDirect Analyse")) {
                        // get the new version, and update the list of file not yet loaded
                        updateNotYetLoaded(mainSettings, reporting, ezPortfolioProxy);

                        allEZModels = new BourseDirectAnalyser(mainSettings).start(reporting, ezPortfolioProxy);
                    }

                    try (Reporting ignored = reporting.pushSection("Vérification des Opérations")) {
                        new EZModelChecker(reporting).validateModels(allEZModels);
                    }
                    Set<ShareValue> shareValues = new HashSet<>();
                    shareValues.addAll(ezPortfolioProxy.getShareValues());
                    shareValues.addAll(serverState.getNewShares().stream().filter(f -> !StringUtils.isBlank(f.getUserShareName())).collect(Collectors.toList()));
                    ShareUtil shareUtil = new ShareUtil(ezPortfolioProxy.getPRU(), shareValues);

                    List<EzReport> allEzReports = new EzEditionExporter(mainSettings, reporting).exportModels(allEZModels, ezPortfolioProxy, shareUtil);
                    updateShareValuesAndEzReports(ezPortfolioProxy.getNewPRUValues(), knownValues, allEzReports);
                });

    }


    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    // upload valid operations into GoogleDriver EZPortfolio
    public EzProcess upload() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        EnumEZBroker courtier = EnumEZBroker.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings,
                "Mise à jour d'EZPortfolio avec les opérations validé",
                ProcessManager.getLog(mainSettings, courtier.getDirName(), "-upload.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
            try (Reporting rep = reporting.pushSection("Mise à jour de EZPortfolio")) {
                Optional<ShareValue> invalidShare = serverState.getNewShares().stream().filter(n -> StringUtils.isBlank(n.getUserShareName())).findFirst();
                if (invalidShare.isPresent()){
                    // if from the previous analysis the newShares are not correctly filled => stop the process
                    reporting.error("La valeur: "+invalidShare.get().getTickerCode()+" n'a pas de nom");
                }
                else {
                    Optional<ShareValue> dirtyShare = serverState.getNewShares().stream().filter(ShareValue::isDirty).findFirst();
                    if (dirtyShare.isPresent()) {
                        // depuis la derniere analyse, le user a changé le nom d'une nouvelle valeur
                        reporting.error("La valeur: " + dirtyShare.get().getTickerCode() + " a changé de nom, vous devez relancer la génération des opérations avant de mettre à jour EzPortfolio");
                    } else {

                        serverState.setEzPortfolioProxy(null); // don't use the cache version when uploading
                        EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings);
                        EZPortfolioProxy ezPortfolioProxy = ezPortfolioManager.load();

                        final EZPortfolioProxy ezPortfolioProxyFinal = ezPortfolioProxy;  // because of the lambda just below
                        // transfert all the new PRU row created in the previous analysis in this new just loaded PRU
                        serverState.getNewPRUs().forEach(pru -> ezPortfolioProxyFinal.getPRU().newPRU(pru));

                        List<EzReport> result = ezPortfolioProxy.save(reporting, serverState.getEzReports());
                        updateShareValuesAndEzReports(new LinkedList<>(), ezPortfolioProxy.getShareValues(), result);

                        // get the new version, and update the list of file not yet loaded
                        updateNotYetLoaded(mainSettings, reporting, ezPortfolioProxy);
                    }
                }
            }
        });
    }

    @GET
    @Path("filesNotLoaded")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess filesNotLoaded() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        EnumEZBroker courtier = EnumEZBroker.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings,
                "Chargement des fichiers non traité",
                ProcessManager.getLog(mainSettings, courtier.getDirName(), "-notLoaded.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
                    EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, mainSettings);
                    EZPortfolioProxy ezPortfolioProxy = ezPortfolioManager.load();
                    updateNotYetLoaded(mainSettings, reporting, ezPortfolioProxy);
                });
    }

    private void updateNotYetLoaded(MainSettings mainSettings, Reporting reporting, EZPortfolioProxy ezPortfolioProxy) throws Exception {
        List<String> notYetLoaded = new BourseDirectAnalyser(mainSettings).getFilesNotYetLoaded(reporting, ezPortfolioProxy);
        serverState.setFilesNotYetLoaded(notYetLoaded);
    }

    private void updateShareValuesAndEzReports(List<String> newPRUs, Set<ShareValue> knownShareValues, List<EzReport> newReports){
        serverState.setEzReports(newReports);
        serverState.setNewPRUs(newPRUs);
        // recupere les valeurs analysé
        Set<ShareValue> newShareValues = newReports.stream()
                                                    .flatMap(r -> r.getEzEditions().stream())
                .flatMap(ezEdition -> ezEdition.getEzPortefeuilleEditions().stream())
                .map(ezPortefeuilleEdition -> new ShareValue(ezPortefeuilleEdition.getTickerGoogleFinance(), ezPortefeuilleEdition.getValeur(), false))
                .collect(Collectors.toSet());
        // fait la soustraction des 2 listes
        newShareValues.removeAll(knownShareValues);
        serverState.setNewShares(newShareValues);
    }

}
