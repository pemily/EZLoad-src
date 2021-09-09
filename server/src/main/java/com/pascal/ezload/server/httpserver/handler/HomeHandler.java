package com.pascal.ezload.server.httpserver.handler;

import com.github.mustachejava.Mustache;
import com.pascal.ezload.server.Main;
import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

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
