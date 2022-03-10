package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.gdrive.GDriveConnection;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.security.AuthManager;
import com.pascal.ezload.service.sources.Reporting;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;


@Path("security")
public class SecurityHandler {
    private static String BAD_PASSWORD = "***";

    @Inject
    private ProcessManager processManager;

    @POST
    @Path("/createLogin")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createUserPassword(
            @NotNull @QueryParam("courtier") EnumEZBroker courtier,
            @NotNull AuthInfo authParam) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        AuthManager authManager = SettingsManager.getAuthManager(mainSettings, ezProfil);
        AuthInfo authInfo = authManager.getAuthInfo(courtier);
        if (authInfo == null){
            authManager.saveAuthInfo(courtier, authParam);
        }
        else {
            authInfo.setUsername(authParam.getUsername());

            if (!StringUtils.isBlank(authParam.getPassword()) && !BAD_PASSWORD.equals(authParam.getPassword())){
                authInfo.setPassword(authParam.getPassword());
            }
            authManager.saveAuthInfo(courtier, authInfo);
        }
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthInfo getAuthWithoutPassword(@NotNull @QueryParam("courtier") EnumEZBroker courtier) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        AuthManager authManager = SettingsManager.getAuthManager(mainSettings, ezProfil);
        AuthInfo result = authManager.getAuthWithoutPassword(courtier);
        if (result == null){
            result = new AuthInfo();
            result.setPassword("");
            result.setPassword("");
        }
        else result.setPassword(BAD_PASSWORD);
        return result;
    }

    @GET
    @Path("/gDriveCheck")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess gDriveCheck() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        return processManager.createNewRunningProcess(mainSettings, ezProfil,
                "Validation du fichier de sécurité Google Drive",
                ProcessManager.getLog(mainSettings, "gDriveValidationSecretFile", ".html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
                    try{
                        GDriveConnection.getService(reporting, SettingsManager.getInstance().getActiveEzProfil(mainSettings).getEzPortfolio().getGdriveCredsFile());
                        // si pas d'exception
                        reporting.info("La connection est validé, vous pouvez utiliser EZLoad");
                    }
                    catch(Exception e){
                        reporting.error("Il y a une erreur soit avec votre fichier de sécurité, soit avec votre url EZPortfolio");
                    }
                });
    }
}
