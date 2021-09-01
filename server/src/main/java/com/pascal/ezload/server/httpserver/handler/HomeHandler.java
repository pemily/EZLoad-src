package com.pascal.ezload.server.httpserver.handler;

import com.github.mustachejava.Mustache;
import com.pascal.ezload.server.Main;
import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.service.config.MainSettings;
import org.apache.http.HttpHeaders;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Path("home")
public class HomeHandler {
    @Inject
    @Named(value= Main.MUSTACHE_HOME_TEMPLATE)
    private Mustache homeTemplateMustache;

    @Context
    private HttpServletResponse context;

    @Inject
    private MainSettings mainSettings;

    @Inject
    EZHttpServer server;


    @GET
    @Path("/home")
    public void home() throws Exception {
        context.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_TYPE.withCharset(StandardCharsets.UTF_8.name()).toString());
        Writer writer = context.getWriter();
        homeTemplateMustache.execute(writer, mainSettings);
        writer.close();
    }


    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping(){
        return "pong";
    }


    @GET
    @Path("/settings")
    @Produces(MediaType.APPLICATION_JSON)
    public MainSettings getSettings(){
        return mainSettings;
    }


    @GET
    @Path("/exit")
    public void exit() throws Exception {
        server.stop();
    }
}
