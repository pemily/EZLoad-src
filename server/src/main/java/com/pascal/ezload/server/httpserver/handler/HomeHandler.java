package com.pascal.ezload.server.httpserver.handler;

import com.github.mustachejava.Mustache;
import com.pascal.ezload.server.Main;
import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import org.apache.http.HttpHeaders;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
    @Path("/saveSettingsOk")
    public void saveSettingsOk(String mainSettings) throws IOException {
        System.out.println("MainSettings4 "+mainSettings);
    }


    @POST
    @Path("/saveSettings4")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveSettings4(MainSettings mainSettings) throws IOException {
        System.out.println("MainSettings4 "+mainSettings);
    }



    @POST
    @Path("/exit")
    public void exit() throws Exception {
        server.stop();
    }
}
