package com.pascal.ezload.server.httpserver.handler;

import java.util.*;
import java.util.stream.Collectors;

import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.EZModelChecker;
import com.pascal.ezload.service.exporter.EzEditionExporter;
import com.pascal.ezload.service.exporter.EZPortfolioManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.*;
import com.pascal.ezload.service.exporter.ezEdition.data.common.AccountData;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectAnalyser;

import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
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
            SettingsManager settingsManager = SettingsManager.getInstance();
            MainSettings mainSettings = settingsManager.loadProps();
            EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
            return processManager.createNewRunningProcess(mainSettings, ezProfil,
                    "Téléchargement des nouvelles opérations de " + courtier.getEzPortfolioName() + " et Analyse",
                    ProcessManager.getLog(mainSettings, courtier.getDirName(), "-downloadAndAnalyze.html"),
                    (processLogger) -> {
                        Reporting reporting = processLogger.getReporting();

                        EZPortfolioProxy ezPortfolioProxy = loadOriginalEzPortfolioProxyOrGetFromCache(ezProfil, reporting);

                        BourseDirectDownloader bourseDirectDownloader = new BourseDirectDownloader(reporting, mainSettings, ezProfil);
                        // Donwload the files, according to the last date retrieved from ezPortfolio
                        bourseDirectDownloader.start(chromeVersion, settingsManager.saveNewChromeDriver(), ezPortfolioProxy);


                        // get the new version, and update the list of file not yet loaded
                        updateNotYetLoaded(mainSettings, ezProfil, reporting, ezPortfolioProxy);
                    });
        }
    }

    private EZPortfolioProxy loadOriginalEzPortfolioProxyOrGetFromCache(EzProfil ezProfil, Reporting reporting) throws Exception {
        EZPortfolioProxy ezPortfolioProxy = serverState.getEzPortfolioProxy();
        if (ezPortfolioProxy == null) {
            EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, ezProfil);
            ezPortfolioProxy = ezPortfolioManager.load();
            serverState.setEzPortfolioProxy(ezPortfolioProxy);
        }
        return ezPortfolioProxy.createDeepCopy();
    }

    @GET
    @Path("/analyze")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess analyze() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        EnumEZBroker courtier = EnumEZBroker.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings, ezProfil,
                "Analyse des nouvelles opérations de " + courtier.getEzPortfolioName(),
                ProcessManager.getLog(mainSettings, courtier.getDirName(), "-analyze.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();

                    EZPortfolioProxy ezPortfolioProxy = loadOriginalEzPortfolioProxyOrGetFromCache(ezProfil, reporting);
                    Set<ShareValue> knownValues = ezPortfolioProxy.getShareValues();

                    List<EZModel> allEZModels;
                    try (Reporting ignored = reporting.pushSection("BourseDirect Analyse")) {
                        // get the new version, and update the list of file not yet loaded
                        updateNotYetLoaded(mainSettings, ezProfil, reporting, ezPortfolioProxy);

                        allEZModels = new BourseDirectAnalyser(mainSettings, ezProfil).start(reporting, ezPortfolioProxy);
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
    @Consumes(MediaType.APPLICATION_JSON)
    // upload valid operations into GoogleDriver EZPortfolio
    public EzProcess upload(@NotNull List<String> ignoreEzEditionId) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        EnumEZBroker courtier = EnumEZBroker.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings, ezProfil,
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
                        EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, ezProfil);
                        EZPortfolioProxy ezPortfolioProxy = ezPortfolioManager.load();

                        final EZPortfolioProxy ezPortfolioProxyFinal = ezPortfolioProxy;  // because of the lambda just below
                        // transfert all the new PRU row created in the previous analysis in this new just loaded PRU
                        serverState.getNewPRUs().forEach(pru -> ezPortfolioProxyFinal.getPRU().newPRU(pru));

                        List<EzReport> result = ezPortfolioProxy.save(ezProfil, reporting, serverState.getEzReports(), ignoreEzEditionId);

                        updateShareValuesAndEzReports(new LinkedList<>(), ezPortfolioProxy.getShareValues(), result);

                        // get the new version, and update the list of file not yet loaded
                        updateNotYetLoaded(mainSettings, ezProfil, reporting, ezPortfolioProxy);
                    }
                }
            }
        });
    }

    @GET
    @Path("filesNotLoaded")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess filesNotLoaded() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        EnumEZBroker courtier = EnumEZBroker.BourseDirect;
        return processManager.createNewRunningProcess(mainSettings, ezProfil,
                "Chargement des fichiers non traité",
                ProcessManager.getLog(mainSettings, courtier.getDirName(), "-notLoaded.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
                    EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, ezProfil);
                    EZPortfolioProxy ezPortfolioProxy = ezPortfolioManager.load();
                    updateNotYetLoaded(mainSettings, ezProfil, reporting, ezPortfolioProxy);
                });
    }

    private void updateNotYetLoaded(MainSettings mainSettings, EzProfil ezProfil, Reporting reporting, EZPortfolioProxy ezPortfolioProxy) throws Exception {
        List<String> notYetLoaded = new BourseDirectAnalyser(mainSettings, ezProfil).getFilesNotYetLoaded(reporting, ezPortfolioProxy);
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

    @DELETE
    @Path("/clearCache")
    @Consumes(MediaType.APPLICATION_JSON)
    public void clearCache() {
        serverState.setEzPortfolioProxy(null);
    }


    @POST
    @Path("/startDate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // input format is: 2021-11-09T22:52:57.346Z
    public EzProcess setStartDate(@NotNull @QueryParam("date") String startDate, @NotNull BourseDirectEZAccountDeclaration account) throws Exception {
        String[] dateTime = com.pascal.ezload.service.util.StringUtils.divide(startDate, 'T');
        if (dateTime == null || dateTime.length != 2){
            throw new IllegalArgumentException("Invalid date format: "+startDate);
        }
        String[] date = dateTime[0].split("-");
        if (date.length != 3){
            throw new IllegalArgumentException("Invalid date format: "+startDate);
        }
        EZDate ezStartDate = EZDate.parseFrenchDate(date[2]+"/"+date[1]+"/"+date[0], '/');

        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);

        return processManager.createNewRunningProcess(mainSettings, ezProfil,
                "Sauvegarde de la date de démarrage: "+ezStartDate.toEzPortoflioDate()+" pour le compte: "+account.getName()+" "+account.getNumber(),
                ProcessManager.getLog(mainSettings, account.getNumber(), "-setStartDate.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();

                    EZPortfolioProxy ezPortfolioProxy = loadOriginalEzPortfolioProxyOrGetFromCache(ezProfil, reporting);
                    EzReport ezReport = new EzReport();
                    EzEdition ezEdition = new EzEdition();
                    RuleDefinitionSummary createStartDateRule = new RuleDefinitionSummary();
                    createStartDateRule.setBroker(null);
                    createStartDateRule.setBrokerFileVersion(-1);
                    createStartDateRule.setName("Date de Départ");
                    ezEdition.setId("DATE_DE_DEPART");
                    ezEdition.setRuleDefinitionSummary(createStartDateRule);
                    EzData ezData = new EzData();
                    ezData.put(AccountData.account_number, account.getNumber());
                    ezEdition.setData(ezData);
                    EzOperationEdition ezOperationEdition = new EzOperationEdition();
                    ezOperationEdition.setDate(ezStartDate.toEzPortoflioDate());
                    ezOperationEdition.setBroker(account.getEzBroker().getEzPortfolioName());
                    ezOperationEdition.setDescription("Date de démarrage d'EZLoad pour le compte: "+account.getName());
                    ezEdition.setEzOperationEditions(Collections.singletonList(ezOperationEdition));
                    ezReport.setEzEditions(Collections.singletonList(ezEdition));
                    clearCache();

                    ezPortfolioProxy.save(ezProfil, reporting, Collections.singletonList(ezReport), new ArrayList<>());

                    reporting.info("Date sauvegardé dans l'onglet 'MesOpérations'");
                });
    }
}
