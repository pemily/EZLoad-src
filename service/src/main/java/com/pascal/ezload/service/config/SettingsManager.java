package com.pascal.ezload.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.model.EnumBRCourtier;
import com.pascal.ezload.service.security.AuthManager;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;

import java.io.*;
import java.util.Random;

public class SettingsManager {
    private final String configFile;

    private SettingsManager(String configFile){
        this.configFile = configFile;
    }

    public MainSettings loadProps() throws Exception {
        MainSettings settings = readConfigFile();
        String passphrase = settings.getEZLoad().getPassPhrase();
        if (passphrase == null){
            settings.getEZLoad().setPassPhrase(AuthManager.getNewRandonmEncryptionPhrase());
            saveConfigFile(settings);
        }
        return settings;
    }

    private MainSettings readConfigFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try(Reader reader = new FileReader(configFile)) {
             return mapper.readValue(reader, MainSettings.class);
        }
    }

    public void saveConfigFile(MainSettings mainSettings) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new FileWriter(configFile), mainSettings);
    }

    public static AuthManager getAuthManager() throws Exception {
        MainSettings mainSettings = getInstance().loadProps();
        return new AuthManager(mainSettings.getEZLoad().getPassPhrase(), mainSettings.getEZLoad().getCourtierCredsFile());
    }

    public static String getDownloadDir(MainSettings mainSettings, EnumBRCourtier brCourtier){
        return mainSettings.getEZLoad().getDownloadDir()+ File.separator+brCourtier.getDirName();
    }

    private static String getConfigFilePath(){
        String configFile = System.getProperty("ezloadfile");
        if (configFile == null) configFile = System.getenv("ezloadfile");
        if (configFile == null){
            String userDir = System.getProperty("user.dir");
            String brDir = userDir + File.separator + "EZLoad";
            new File(brDir).mkdirs();
            configFile = brDir + File.separator + "ezload-config.yaml";
        }
        return configFile;
    }

    public static SettingsManager getInstance() throws IOException {
        String configFilePath = getConfigFilePath();
        if (new File(configFilePath).exists()){
            return new SettingsManager(configFilePath);
        }
        SettingsManager settingsManager = new SettingsManager(configFilePath);
        settingsManager.saveConfigFile(getInitialSettings(configFilePath));
        return settingsManager;
    }

    private static MainSettings getInitialSettings(String configFilePath) {
        MainSettings mainSettings = new MainSettings();
        MainSettings.EZLoad ezLoad = new MainSettings.EZLoad();
        String ezHome = new File(configFilePath).getParentFile().getAbsolutePath();
        ezLoad.setLogsDir(ezHome+File.separator+"logs");
        ezLoad.setDownloadDir(ezHome+File.separator+"courtiers");
        ezLoad.setPassPhrase(genString(42));
        ezLoad.setCourtierCredsFile(ezHome+File.separator+"ezCreds.json");
        mainSettings.setEZLoad(ezLoad);

        MainSettings.ChromeSettings chromeSettings = new MainSettings.ChromeSettings();
        chromeSettings.setUserDataDir(ezHome+File.separator+"chrome-datadir");
        chromeSettings.setDefaultTimeout(20);
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
