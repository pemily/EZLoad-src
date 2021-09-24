package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.WebData;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumBRCourtier;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectBRAccountDeclaration;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectSearchAccounts;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
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
        return new WebData(SettingsManager.getInstance().loadProps().validate(), processManager.getLatestProcess());
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
    public boolean searchAccounts(@NotNull @QueryParam("courtier") EnumBRCourtier courtier) throws Exception {
        if (courtier != EnumBRCourtier.BourseDirect) {
            throw new IllegalArgumentException("Cette operation n'est pas encore développé pour le courtier: "+courtier.getEzPortfolioName());
        }
        else {
            MainSettings mainSettings = SettingsManager.getInstance().loadProps();
            return processManager.createNewRunningProcess(mainSettings, ProcessManager.getLog(mainSettings, courtier.getDirName(), "-searchAccount.log"),
                (processLogger) -> {
                    List<BourseDirectBRAccountDeclaration> accountsExtracted = new BourseDirectSearchAccounts(mainSettings, processLogger.getReporting()).extract();
                    // copy the accounts into the main settings
                    List<BourseDirectBRAccountDeclaration> accountsDefined = mainSettings.getBourseDirect().getAccounts();
                    processLogger.getReporting().info(accountsDefined.size()+" Compte(s) trouvé(s)");
                    accountsExtracted.stream()
                            .filter(acc ->
                                    accountsDefined
                                            .stream()
                                            .noneMatch(accDefined -> accDefined.getNumber().equals(acc.getNumber())))
                            .forEach(newAccount -> {
                                        processLogger.getReporting().info("Ajout du compte: "+newAccount.getNumber()+" de "+newAccount.getName());
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
    public void viewLogProcess(@NotNull @QueryParam("log") String log) throws IOException {
        processManager.viewLogProcess(log, response.getWriter());
    }


    @GET
    @Path("/test1")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public boolean test1() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        return processManager.createNewRunningProcess(mainSettings, ProcessManager.getLog(mainSettings, "test1", "-searchAccount.log"),
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
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public boolean test2() throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        return processManager.createNewRunningProcess(mainSettings, ProcessManager.getLog(mainSettings, "test2", "-searchAccount.log"),
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
