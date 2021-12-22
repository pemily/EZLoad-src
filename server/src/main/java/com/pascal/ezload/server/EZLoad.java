package com.pascal.ezload.server;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.util.HttpUtil;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;


public class EZLoad {

    public static int VERSION = 1;

    public static void main(String args[]) throws Exception {
        System.out.println("Configuration file: "+ SettingsManager.getConfigFilePath());

        int port = SettingsManager.getInstance().loadProps().getEzLoad().getPort();
        String homePage = "http://localhost:"+port;

        try {
            EZHttpServer server = new EZHttpServer();
            server.start(port, new AbstractBinder() {
                @Override
                protected void configure() {
                    EzServerState serverState = new EzServerState();
                    bind(server).to(EZHttpServer.class);
                    bind(new ProcessManager(server, serverState)).to(ProcessManager.class);
                    bind(serverState).to(EzServerState.class);
                    server.killIfNoActivity(Duration.ofMinutes(1), serverState);
                }
            });
        }
        catch(Exception e){
            // if the port is buzy, perhaps it is already launched
            // in this case, print, and relaunch a browser
            // TODO: faire un check sur le ping, si ca ne retourne pas pong => dialog swing pour dire de changer le port + display exception?
            String content = HttpUtil.urlContent(homePage+"/api/home/ping");
            if ("pong".equals(content)){
                openPage(homePage);
            }
            throw e;
        }
        openPage(homePage);
    }

    public static void openPage(String homePage) throws URISyntaxException, IOException {
        System.out.println("EZLoad: "+homePage);
        Desktop.getDesktop().browse(new URI(homePage));
    }

}
