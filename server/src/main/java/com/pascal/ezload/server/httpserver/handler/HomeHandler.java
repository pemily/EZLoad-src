package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.WebData;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumEZCourtier;
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

@Path("home")
public class HomeHandler {
    @Context
    private HttpServletResponse response;

    @Inject
    EZHttpServer server;

    @Inject
    private ProcessManager processManager;

    @GET
    @Path("/ping")
    public String ping(){
        return "pong";
    }


    @GET
    @Path("/main")
    @Produces(MediaType.APPLICATION_JSON)
    public WebData getMainData() throws Exception {
        return new WebData(SettingsManager.getInstance().loadProps().validate(),
                            processManager.getLatestProcess(),
                            processManager.isProcessRunning());
    }

    @POST
    @Path("/saveSettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MainSettings saveSettings(MainSettings mainSettings) throws IOException {
        SettingsManager.getInstance().saveConfigFile(mainSettings);
        return mainSettings.validate();
    }

    @GET
    @Path("/searchAccounts")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess searchAccounts(@NotNull @QueryParam("courtier") EnumEZCourtier courtier,
                                    @NotNull @QueryParam("chromeVersion") String chromeVersion) throws Exception {
        if (courtier != EnumEZCourtier.BourseDirect) {
            throw new IllegalArgumentException("Cette operation n'est pas encore développé pour le courtier: "+courtier.getEzPortfolioName());
        }
        else {
            MainSettings mainSettings = SettingsManager.getInstance().loadProps();
            return processManager.createNewRunningProcess(mainSettings,
                    "Recherche de Nouveaux Comptes "+courtier.getEzPortfolioName(),
                    ProcessManager.getLog(mainSettings, courtier.getDirName(), "-searchAccount.log"),
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
        Writer writer = response.getWriter();
        processManager.viewLogProcess(writer);
        writer.close();
    }


    @GET
    @Path("/test1")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess test1() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        return processManager.createNewRunningProcess(mainSettings, "Test1", ProcessManager.getLog(mainSettings, "test1", "-searchAccount.log"),
                (processLogger) -> {
                    for (int i = 0; i < 20; i++) {
                        processLogger.getReporting().info("Bonjour " + i + "\n");
                        System.out.println("Bonjour " + i);
                        sleep();
                    }
                });
    }

    @GET
    @Path("/test2")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess test2() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        return processManager.createNewRunningProcess(mainSettings, "Test2", ProcessManager.getLog(mainSettings, "test2", "-searchAccount.log"),
                (processLogger) -> {
                    for (int i = 0; i < 60; i++) {
                        processLogger.getReporting().info("Bonjour " + i + "\n");
                        System.out.println("Bonjour " + i);
                        sleep();
                    }
                });
    }

    private void sleep(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
