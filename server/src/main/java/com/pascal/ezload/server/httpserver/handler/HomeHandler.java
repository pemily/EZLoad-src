package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.WebData;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectSearchAccounts;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

@Path("home")
public class HomeHandler {
    @Context
    private HttpServletResponse response;

    @Inject
    private EZHttpServer server;

    @Inject
    private ProcessManager processManager;

    @Inject
    private EzServerState ezServerState;

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping(){
        return "pong";
    }

    @GET
    @Path("/main")
    @Produces(MediaType.APPLICATION_JSON)
    public WebData getMainData() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        return new WebData(SettingsManager.searchConfigFilePath(),
                            mainSettings,
                             ezProfil,
                            processManager.getLatestProcess(),
                            ezServerState.isProcessRunning(),
                            ezServerState.getEzReports(),
                            ezServerState.getNewShares(),
                            ezServerState.getFilesNotYetLoaded(),
                            new RulesManager(mainSettings).getAllRules()
                                    .stream()
                                    .map(e -> (RuleDefinitionSummary)e)
                                    .collect(Collectors.toList()),
                            SettingsManager.getVersion()
                );
    }

    @POST
    @Path("/saveMainSettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MainSettings saveMainSettings(MainSettings mainSettings) throws IOException {
        SettingsManager settingsManager = SettingsManager.getInstance();
        settingsManager.saveMainSettingsFile(mainSettings);
        return mainSettings.validate();
    }

    @POST
    @Path("/saveEzProfil")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EzProfil saveEzProfil(EzProfil ezProfil) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        String ezProfilName = settingsManager.loadProps().getActiveEzProfilFilename();
        settingsManager.saveEzProfilFile(ezProfilName, ezProfil);
        return ezProfil.validate();
    }

    @POST
    @Path("/saveNewShareValue")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveNewShareValue(ShareValue shareValue) {
        ezServerState.getNewShares().stream()
            .filter(s -> s.getTickerCode().equals(shareValue.getTickerCode()))
            .forEach(s -> {
                s.setUserShareName(shareValue.getUserShareName());
                s.setDirty(true);
            });
    }

    @GET
    @Path("/searchAccounts")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess searchAccounts(@NotNull @QueryParam("courtier") EnumEZBroker courtier,
                                    @NotNull @QueryParam("chromeVersion") String chromeVersion) throws Exception {
        if (courtier != EnumEZBroker.BourseDirect) {
            throw new IllegalArgumentException("Cette operation n'est pas encore développé pour le courtier: "+courtier.getEzPortfolioName());
        }
        else {
            SettingsManager settingsManager = SettingsManager.getInstance();
            MainSettings mainSettings = settingsManager.loadProps();
            EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
            return processManager.createNewRunningProcess(mainSettings, ezProfil,
                    "Recherche de Nouveaux Comptes "+courtier.getEzPortfolioName(),
                    ProcessManager.getLog(mainSettings, courtier.getDirName(), "-searchAccount.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
                    List<BourseDirectEZAccountDeclaration> accountsExtracted =
                            new BourseDirectSearchAccounts(mainSettings, ezProfil, reporting).extract(chromeVersion, settingsManager.saveNewChromeDriver());
                    // copy the accounts into the main settings

                    List<BourseDirectEZAccountDeclaration> accountsDefined = ezProfil.getBourseDirect().getAccounts();
                    reporting.info(accountsDefined.size()+" Compte(s) trouvé(s)");
                    accountsExtracted.stream()
                            .filter(acc ->
                                    accountsDefined
                                            .stream()
                                            .noneMatch(accDefined -> accDefined.getNumber().equals(acc.getNumber())))
                            .forEach(newAccount -> {
                                        reporting.info("Ajout du compte: "+newAccount.getNumber()+" de "+newAccount.getName());
                                        ezProfil.getBourseDirect().getAccounts().add(newAccount);
                                    });
                    settingsManager.saveEzProfilFile(mainSettings.getActiveEzProfilFilename(), ezProfil);
                }
            );
        }
    }

    @POST
    @Path("/exit")
    public void exit() {
        processManager.kill();
        server.stop();
    }


    @GET
    @Path("/viewProcess")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public void viewLogProcess() throws IOException {
        try(Writer writer = response.getWriter()) {
            processManager.viewLogProcess(writer);
        }
    }

}
