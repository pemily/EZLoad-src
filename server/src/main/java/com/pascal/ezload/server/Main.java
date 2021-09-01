package com.pascal.ezload.server;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.Random;
import java.util.stream.IntStream;


public class Main {

    public static final String MUSTACHE_HOME_TEMPLATE = "homeTemplate";
    public static final String MUSTACHE_DIR_TEMPLATE = "dirTemplate";

    public static void main(String args[]) throws Exception {
        String configFile = args.length > 0 ? args[0] : null;
        if (configFile == null) {
            String userDir = System.getProperty("user.dir");
            String brDir = userDir + File.separator + "EZLoad";
            new File(brDir).mkdirs();
            configFile = brDir + File.separator + "ezload.yaml";
            if (!new File(configFile).exists()){
                SettingsManager settingsManager = new SettingsManager(configFile);
                settingsManager.saveConfigFile(getInitialSettings());
            }
        }

        SettingsManager settingsManager = new SettingsManager(configFile);
        final MainSettings mainSettings = settingsManager.loadProps();

        // https://www.baeldung.com/mustache
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        Mustache homeTemplateMustache = mustacheFactory.compile("templates/home.mustache");
        Mustache dirTemplateMustache = mustacheFactory.compile("templates/dir.mustache");

        EZHttpServer server = new EZHttpServer();
        int port = server.start(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mainSettings).to(MainSettings.class);
                bind(server).to(EZHttpServer.class);
                bind(homeTemplateMustache).named(MUSTACHE_HOME_TEMPLATE).to(Mustache.class);
                bind(dirTemplateMustache).named(MUSTACHE_DIR_TEMPLATE).to(Mustache.class);
            }
        });
        String homePage = "http://localhost:"+port+"/EZLoad/api/home";
        System.out.println("HomePage: "+homePage);
        System.out.println("ApiPage: http://localhost:"+port+"/EZLoad/api/home/settings");
        Desktop.getDesktop().browse(new URI(homePage));

    }

    private static MainSettings getInitialSettings() {
        MainSettings mainSettings = new MainSettings();
        MainSettings.EZLoad ezLoad = new MainSettings.EZLoad();
        String ezHome = System.getProperty("user.dir") + File.separator + "ezload";
        ezLoad.setLogsDir(ezHome+File.separator+"logs");
        ezLoad.setDownloadDir(ezHome+File.separator+"courtiers");
        ezLoad.setPassPhrase(genString(42));
        ezLoad.setCourtierCredsFile(ezHome+File.separator+"ezCreds.json");
        mainSettings.setEZLoad(ezLoad);

        MainSettings.ChromeSettings chromeSettings = new MainSettings.ChromeSettings();
        chromeSettings.setUserDataDir(ezHome+File.separator+"chrome-datadir");
        mainSettings.setChrome(chromeSettings);

        EZPortfolioSettings ezPortfolioSettings = new EZPortfolioSettings();
        mainSettings.setEzPortfolio(ezPortfolioSettings);

        BourseDirectSettings bourseDirectSettings = new BourseDirectSettings();
        mainSettings.setBourseDirect(bourseDirectSettings);

        return mainSettings;
    }

    public static String genString(int length) {
        return new Random().ints(48,123)
                .filter(i -> (i < 58) || (i > 64 && i < 91) || (i > 96))
                .mapToObj(i -> (char) i)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

}
