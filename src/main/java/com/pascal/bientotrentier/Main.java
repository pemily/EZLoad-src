package com.pascal.bientotrentier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.bientotrentier.server.MiniHttpServer;

import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;


public class Main {

    public static void main(String args[]) throws Exception {
        MainSettings mainSettings = loadProps();
        MiniHttpServer server = new MiniHttpServer();
        int port = server.start(mainSettings);
        Desktop.getDesktop().browse(new URI("http://localhost:"+port+"/home"));
    }

    private static MainSettings loadProps() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MainSettings settings = mapper.readValue(new FileReader("src/main/resources/bientotRentier.yaml"), MainSettings.class);
        return settings;
    }

}
