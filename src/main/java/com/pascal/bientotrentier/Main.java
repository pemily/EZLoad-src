package com.pascal.bientotrentier;

import com.pascal.bientotrentier.server.MiniHttpServer;

import java.awt.*;
import java.net.URI;


public class Main {


    public static void main(String args[]) throws Exception {
        SettingsManager settingsManager = new SettingsManager("src/main/resources/bientotRentier.yaml");
        MainSettings mainSettings = settingsManager.loadProps();

        MiniHttpServer server = new MiniHttpServer();
        int port = server.start(mainSettings);
        Desktop.getDesktop().browse(new URI("http://localhost:"+port+"/home"));
    }

}
