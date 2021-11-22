package com.pascal.ezload.server;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.SettingsManager;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import java.awt.*;
import java.net.URI;
import java.time.Duration;


public class EZLoad {

    public static int VERSION = 1;

    public static void main(String args[]) throws Exception {
        System.out.println("Configuration file: "+ SettingsManager.getConfigFilePath());

        EZHttpServer server = new EZHttpServer();
        int port = server.start(new AbstractBinder() {
            @Override
            protected void configure() {
                EzServerState serverState = new EzServerState();
                bind(server).to(EZHttpServer.class);
                bind(new ProcessManager(server, serverState)).to(ProcessManager.class);
                bind(serverState).to(EzServerState.class);
                server.killIfNoActivity(Duration.ofMinutes(1), serverState);
            }
        });
        String homePage = "http://localhost:"+port;
        System.out.println("EZLoad: "+homePage);
        Desktop.getDesktop().browse(new URI(homePage));

    }


}
