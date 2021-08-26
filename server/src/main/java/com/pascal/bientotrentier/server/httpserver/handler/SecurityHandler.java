package com.pascal.bientotrentier.server.httpserver.handler;

import com.pascal.bientotrentier.service.SettingsManager;
import com.pascal.bientotrentier.service.config.MainSettings;
import com.pascal.bientotrentier.service.model.EnumBRCourtier;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("security")
public class SecurityHandler {

    @Inject
    private MainSettings mainSettings;

    @POST
    @Path("/createLogin")
    public void createUserPassword(
            @NotNull @QueryParam("user") String user,
            @NotNull @QueryParam("password") String password,
            @NotNull @QueryParam("courtier") EnumBRCourtier courtier){
        if (!StringUtils.isBlank(user) && !StringUtils.isBlank(password) && courtier != null){
            MainSettings.AuthInfo authInfo = new MainSettings.AuthInfo();
            authInfo.setPassword(password);
            authInfo.setUsername(user);
            try {
                SettingsManager.getAuthManager(mainSettings)
                        .addAuthInfo(courtier, authInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
