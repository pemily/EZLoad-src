package com.pascal.bientotrentier;

import com.pascal.bientotrentier.config.MainSettings;
import com.pascal.bientotrentier.server.BRHttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.awt.*;
import java.net.URI;


public class Main {


    public static void main(String args[]) throws Exception {
        SettingsManager settingsManager = new SettingsManager("src/main/resources/bientotRentier.yaml");
        final MainSettings mainSettings = settingsManager.loadProps();


        BRHttpServer server = new BRHttpServer();
        int port = server.start(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mainSettings).to(MainSettings.class);
                bind(server).to(BRHttpServer.class);
            }
        });
        Desktop.getDesktop().browse(new URI("http://localhost:"+port+"/BientotRentier/api/exit2"));
        // server.waitEnd();
        System.out.println("CLOSE");
    }

}
