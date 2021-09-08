package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumBRCourtier;
import com.pascal.ezload.service.security.AuthManager;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("security")
public class SecurityHandler {

    @POST
    @Path("/createLogin")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createUserPassword(
            @NotNull @QueryParam("courtier") EnumBRCourtier courtier,
            @NotNull AuthInfo authParam) throws Exception {

        AuthManager authManager = SettingsManager.getAuthManager();
        AuthInfo authInfo = authManager.getAuthInfo(courtier);
        if (authInfo == null){
            authManager.saveAuthInfo(courtier, authParam);
        }
        else {
            if (!StringUtils.isBlank(authParam.getUsername())) {
                authInfo.setUsername(authParam.getUsername());
            }
            if (!StringUtils.isBlank(authParam.getPassword())){
                authInfo.setPassword(authParam.getPassword());
            }
            authManager.saveAuthInfo(courtier, authInfo);
        }
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthInfo getAuthWithDummyPassword(@NotNull @QueryParam("courtier") EnumBRCourtier courtier) throws Exception {
        return SettingsManager.getAuthManager().getAuthWithDummyPassword(courtier);
    }
}
