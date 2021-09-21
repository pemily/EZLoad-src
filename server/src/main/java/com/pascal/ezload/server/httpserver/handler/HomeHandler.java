package com.pascal.ezload.server.httpserver.handler;

import java.io.IOException;
import java.util.List;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.WebData;
import com.pascal.ezload.server.httpserver.exec.HttpProcessRunner;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;

import com.pascal.ezload.service.model.EnumBRCourtier;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectBRAccountDeclaration;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectSearchAccounts;
import com.pascal.ezload.service.util.HtmlReporting;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("home")
public class HomeHandler {
    @Context
    private HttpServletResponse context;

    @Inject
    private ProcessManager processManager;

    @Inject
    EZHttpServer server;

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
    public MainSettings saveSettings(MainSettings mainSettings) throws IOException {
        SettingsManager.getInstance().saveConfigFile(mainSettings);
        return mainSettings.validate();
    }

    @GET
    @Path("/searchAccounts")
    @Produces(MediaType.TEXT_HTML)
    public void searchAccounts(@NotNull @QueryParam("courtier") EnumBRCourtier courtier) throws Exception {
        if (courtier != EnumBRCourtier.BourseDirect) {
            HtmlReporting htmlReporting = new HtmlReporting(null, context.getWriter());
            htmlReporting.writeHeader(htmlReporting.escape("Cette operation n'est pas encore développé pour le courtier: "+courtier.getEzPortfolioName()));
            htmlReporting.close();
        }
        else {
            MainSettings mainSettings = SettingsManager.getInstance().loadProps();
            if (processManager.createNewRunningProcess(ProcessManager.getLog(mainSettings, courtier.getDirName(), "-searchAccount.log"))) {
                try (HttpProcessRunner processLogger = new HttpProcessRunner(processManager.getLatestProcess(), context.getWriter(), server.fileLinkCreator(mainSettings))) {
                    try {
                        List<BourseDirectBRAccountDeclaration> accountsExtracted = new BourseDirectSearchAccounts(mainSettings, processLogger.getReporting()).extract();
                        // copy the accounts into the main settings
                        List<BourseDirectBRAccountDeclaration> accountsDefined = mainSettings.getBourseDirect().getAccounts();
                        accountsExtracted.stream()
                                .filter(acc ->
                                        !accountsDefined
                                                .stream()
                                                .anyMatch(accDefined -> accDefined.getNumber().equals(acc.getNumber())))
                                .forEach(newAccount ->
                                        mainSettings.getBourseDirect().getAccounts().add(newAccount)
                                );
                        SettingsManager.getInstance().saveConfigFile(mainSettings);
                    }
                    catch(Exception e){
                        processLogger.getReporting().error(e);
                    }
                }
            } else {
                HtmlReporting htmlReporting = new HtmlReporting(null, context.getWriter());
                htmlReporting.writeHeader(htmlReporting.escape("EZLoad est déjà occupé avec une autre opération"));
                htmlReporting.close();
            }
        }
    }

    @POST
    @Path("/exit")
    public void exit() throws Exception {
        server.stop();
    }
}
