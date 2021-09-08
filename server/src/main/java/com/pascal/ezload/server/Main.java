package com.pascal.ezload.server;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

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
        System.out.println("HomePage: "+homePage);
        System.out.println("ApiPage: http://localhost:"+port+"/EZLoad/api/home/settings");
        Desktop.getDesktop().browse(new URI(homePage));

    }


}
