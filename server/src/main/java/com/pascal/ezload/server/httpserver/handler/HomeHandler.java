package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.WebData;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.EZPortfolioManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectAnalyser;
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
        MainSettings mainSettings = SettingsManager.getInstance().loadProps().validate();

        return new WebData(mainSettings,
                            processManager.getLatestProcess(),
                            ezServerState.isProcessRunning(),
                            ezServerState.getEzReports(),
                            ezServerState.getNewShares(),
                            ezServerState.getFilesNotYetLoaded(),
                            new RulesManager(mainSettings).getAllRules()
                                    .stream()
                                    .map(e -> (RuleDefinitionSummary)e)
                                    .collect(Collectors.toList())
                );
    }

    @POST
    @Path("/saveSettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MainSettings saveSettings(MainSettings mainSettings) throws IOException {
        SettingsManager.getInstance().saveConfigFile(mainSettings);
        return mainSettings.validate();
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
            MainSettings mainSettings = SettingsManager.getInstance().loadProps();
            return processManager.createNewRunningProcess(mainSettings,
                    "Recherche de Nouveaux Comptes "+courtier.getEzPortfolioName(),
                    ProcessManager.getLog(mainSettings, courtier.getDirName(), "-searchAccount.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
                    List<BourseDirectEZAccountDeclaration> accountsExtracted =
                            new BourseDirectSearchAccounts(mainSettings, reporting).extract(chromeVersion, SettingsManager.saveNewChromeDriver());
                    // copy the accounts into the main settings
                    List<BourseDirectEZAccountDeclaration> accountsDefined = mainSettings.getBourseDirect().getAccounts();
                    reporting.info(accountsDefined.size()+" Compte(s) trouvé(s)");
                    accountsExtracted.stream()
                            .filter(acc ->
                                    accountsDefined
                                            .stream()
                                            .noneMatch(accDefined -> accDefined.getNumber().equals(acc.getNumber())))
                            .forEach(newAccount -> {
                                        reporting.info("Ajout du compte: "+newAccount.getNumber()+" de "+newAccount.getName());
                                        mainSettings.getBourseDirect().getAccounts().add(newAccount);
                                    });
                    SettingsManager.getInstance().saveConfigFile(mainSettings);
                }
            );
        }
    }

    @POST
    @Path("/exit")
    public void exit() throws Exception {
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
