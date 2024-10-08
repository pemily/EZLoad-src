/**
 * ezServer - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.gdrive.GDriveConnection;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.security.AuthManager;
import com.pascal.ezload.common.sources.Reporting;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;


@Path("security")
public class SecurityHandler {

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
        AuthManager authManager = settingsManager.getAuthManager(mainSettings);
        AuthInfo authInfo = authManager.getAuthInfo(courtier);
        if (authInfo == null){
            authManager.saveAuthInfo(courtier, authParam);
        }
        else {
            authInfo.setUsername(authParam.getUsername());

            if (!StringUtils.isBlank(authParam.getPassword()) && !isBadPassword(authParam.getPassword())){
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
        AuthManager authManager = settingsManager.getAuthManager(mainSettings);
        AuthInfo result = authManager.getAuthInfo(courtier);
        if (result == null){
            result = new AuthInfo();
            result.setUsername("");
            result.setPassword("");
        }
        else result.setPassword(genBadPassword(result.getPassword().length()));
        return result;
    }

    @GET
    @Path("/gDriveCheck")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess gDriveCheck() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        return processManager.createNewRunningProcess(settingsManager, mainSettings,
                "Validation du fichier de sécurité Google Drive",
                ProcessManager.getLog(mainSettings, "gDriveValidationSecretFile", ".html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
                    try{
                        GDriveConnection.getService(reporting, settingsManager.getGDriveCredsFile(mainSettings.getActiveEzProfilName()));
                        // si pas d'exception
                        reporting.info("La connection est validé, vous pouvez utiliser EZLoad");
                    }
                    catch(Exception e){
                        reporting.error("Il y a une erreur soit avec votre fichier de sécurité, soit avec votre url EZPortfolio");
                    }
                });
    }

    // is bad if it contains only * characters
    private boolean isBadPassword(String pwd){
        return pwd.chars().allMatch(c -> c == '*');
    }

    private String genBadPassword(int length){
        StringBuilder s = new StringBuilder();
        for (int i = 0; i <length; i++){
            s.append("*");
        }
        return s.toString();
    }
}
