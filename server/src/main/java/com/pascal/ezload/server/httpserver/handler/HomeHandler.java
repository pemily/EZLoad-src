package com.pascal.ezload.server.httpserver.handler;

import java.io.IOException;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("home")
public class HomeHandler {
    @Context
    private HttpServletResponse context;

    @Inject
    EZHttpServer server;

    @GET
    @Path("/ping")
    public String ping(){
        return "pong";
    }


    @GET
    @Path("/settings")
    @Produces(MediaType.APPLICATION_JSON)
    public MainSettings getSettings() throws Exception {
        return SettingsManager.getInstance().loadProps();
    }

    @POST
    @Path("/saveSettings")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveSettings(MainSettings mainSettings) throws IOException {
        SettingsManager.getInstance().saveConfigFile(mainSettings);
    }

    @POST
    @Path("/exit")
    public void exit() throws Exception {
        server.stop();
    }
}
