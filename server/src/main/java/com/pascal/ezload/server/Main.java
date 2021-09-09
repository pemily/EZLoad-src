package com.pascal.ezload.server;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.service.config.SettingsManager;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import java.awt.*;
import java.net.URI;


public class Main {

    public static void main(String args[]) throws Exception {

        EZHttpServer server = new EZHttpServer();
        int port = server.start(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(server).to(EZHttpServer.class);
            }
        });
        String homePage = "http://localhost:"+port+"/EZLoad/api/home";
        System.out.println("Configuration file: "+ SettingsManager.getConfigFilePath());
        System.out.println("HomePage: "+homePage);
        System.out.println("ApiPage: http://localhost:"+port+"/EZLoad/api/home/settings");
        Desktop.getDesktop().browse(new URI(homePage));

    }


}
