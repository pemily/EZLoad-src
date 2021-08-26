package com.pascal.bientotrentier.server;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.pascal.bientotrentier.server.httpserver.BRHttpServer;
import com.pascal.bientotrentier.service.SettingsManager;
import com.pascal.bientotrentier.service.config.MainSettings;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.awt.*;
import java.net.URI;


public class Main {

    public static final String MUSTACHE_HOME_TEMPLATE = "homeTemplate";
    public static final String MUSTACHE_DIR_TEMPLATE = "dirTemplate";

    public static void main(String args[]) throws Exception {
        SettingsManager settingsManager = new SettingsManager("src/main/resources/bientotRentier.yaml");
        final MainSettings mainSettings = settingsManager.loadProps();

        // https://www.baeldung.com/mustache
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        Mustache homeTemplateMustache = mustacheFactory.compile("templates/home.mustache");
        Mustache dirTemplateMustache = mustacheFactory.compile("templates/dir.mustache");

        BRHttpServer server = new BRHttpServer();
        int port = server.start(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mainSettings).to(MainSettings.class);
                bind(server).to(BRHttpServer.class);
                bind(homeTemplateMustache).named(MUSTACHE_HOME_TEMPLATE).to(Mustache.class);
                bind(dirTemplateMustache).named(MUSTACHE_DIR_TEMPLATE).to(Mustache.class);
            }
        });
        Desktop.getDesktop().browse(new URI("http://localhost:"+port+"/BientotRentier/api/home"));

    }

}
