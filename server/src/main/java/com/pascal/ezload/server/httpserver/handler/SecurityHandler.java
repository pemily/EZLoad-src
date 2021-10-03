package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumEZCourtier;
import com.pascal.ezload.service.security.AuthManager;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;


@Path("security")
public class SecurityHandler {

    @POST
    @Path("/createLogin")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createUserPassword(
            @NotNull @QueryParam("courtier") EnumEZCourtier courtier,
            @NotNull AuthInfo authParam) throws Exception {

        AuthManager authManager = SettingsManager.getAuthManager();
        AuthInfo authInfo = authManager.getAuthInfo(courtier);
        if (authInfo == null){
            authManager.saveAuthInfo(courtier, authParam);
        }
        else {
            authInfo.setUsername(authParam.getUsername());

            if (!StringUtils.isBlank(authParam.getPassword())){
                authInfo.setPassword(authParam.getPassword());
            }
            authManager.saveAuthInfo(courtier, authInfo);
        }
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthInfo getAuthWithDummyPassword(@NotNull @QueryParam("courtier") EnumEZCourtier courtier) throws Exception {
        return SettingsManager.getAuthManager().getAuthWithDummyPassword(courtier);
    }
}
