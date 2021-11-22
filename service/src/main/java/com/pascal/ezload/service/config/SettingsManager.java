package com.pascal.ezload.service.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.security.AuthManager;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.util.FileUtil;

public class SettingsManager {
    private final String configFile;
    public final static String EZPORTFOLIO_GDRIVE_URL_PREFIX = "https://docs.google.com/spreadsheets/d/";

    private SettingsManager(String configFile){
        this.configFile = configFile;
    }

    public MainSettings loadProps() throws Exception {
        MainSettings settings = readConfigFile();
        String passphrase = settings.getEzLoad().getPassPhrase();
        if (passphrase == null){
            settings.getEzLoad().setPassPhrase(AuthManager.getNewRandonmEncryptionPhrase());
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
        mainSettings.clearErrors();
        mapper.writeValue(new FileWriter(configFile), mainSettings);
    }

    public static AuthManager getAuthManager() throws Exception {
        MainSettings mainSettings = getInstance().loadProps();
        return new AuthManager(mainSettings.getEzLoad().getPassPhrase(), mainSettings.getEzLoad().getCourtierCredsFile());
    }

    public static String getDownloadDir(MainSettings mainSettings, EnumEZBroker brCourtier){
        return mainSettings.getEzLoad().getDownloadDir()+ File.separator+brCourtier.getDirName();
    }

    public static String getConfigFilePath(){
        String configFile = System.getProperty("ezloadConfig");
        if (configFile == null) configFile = System.getenv("ezloadConfig");
        if (configFile == null){
            String userDir = System.getProperty("user.home");
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

    public static Consumer<String> saveNewChromeDriver(){
        return newChromeDriver -> {
            try {
                SettingsManager manager = getInstance();
                MainSettings mainSettings = manager.loadProps();
                mainSettings.getChrome().setDriverPath(newChromeDriver);
                manager.saveConfigFile(mainSettings);
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
        };
    }

    private static MainSettings getInitialSettings(String configFilePath) throws IOException {
        String credsDir = "creds";

        MainSettings mainSettings = new MainSettings();
        MainSettings.EZLoad ezLoad = new MainSettings.EZLoad();
        String ezHome = new File(configFilePath).getParentFile().getAbsolutePath();
        MainSettings.Admin admin = new MainSettings.Admin();
        admin.setShowRules(false);
        ezLoad.setAdmin(admin);
        ezLoad.setLogsDir(ezHome+File.separator+"logs");
        ezLoad.setDownloadDir(ezHome+File.separator+"courtiers");
        ezLoad.setRulesDir(ezHome+File.separator+"rules");
        ezLoad.setPassPhrase(genString(42));
        ezLoad.setCourtierCredsFile(ezHome+File.separator+credsDir+File.separator+"ezCreds.json");

        new File(ezLoad.getDownloadDir()).mkdirs();
        new File(ezLoad.getRulesDir()).mkdirs();
        new File(ezLoad.getLogsDir()).mkdirs();
        new File(ezLoad.getCourtierCredsFile()).getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(ezLoad.getCourtierCredsFile());
        output.write("{}".getBytes(StandardCharsets.UTF_8));
        output.close();
        mainSettings.setEzLoad(ezLoad);

        MainSettings.ChromeSettings chromeSettings = new MainSettings.ChromeSettings();
        chromeSettings.setUserDataDir(ezHome+File.separator+"chrome"+File.separator+"data");
        // chromeDriver is a file that does not exists, so next time it will be downloaded
        chromeSettings.setDriverPath(ezHome+File.separator+"chrome"+File.separator+"driver"+File.separator+"chromedriver");
        chromeSettings.setDefaultTimeout(20);
        new File(chromeSettings.getUserDataDir()).mkdirs();
        new File(chromeSettings.getDriverPath()).getParentFile().mkdirs();
        mainSettings.setChrome(chromeSettings);

        EZPortfolioSettings ezPortfolioSettings = new EZPortfolioSettings();
        mainSettings.setEzPortfolio(ezPortfolioSettings);
        ezPortfolioSettings.setEzPortfolioUrl(EZPORTFOLIO_GDRIVE_URL_PREFIX);
        ezPortfolioSettings.setGdriveCredsFile(ezHome+File.separator+credsDir+File.separator+"gdrive-access.json");
        new File(ezPortfolioSettings.getGdriveCredsFile()).getParentFile().mkdirs();
        output = new FileOutputStream(ezPortfolioSettings.getGdriveCredsFile());
        output.write("{}".getBytes(StandardCharsets.UTF_8));
        output.close();
        mainSettings.setEzLoad(ezLoad);

        BourseDirectSettings bourseDirectSettings = new BourseDirectSettings();
        bourseDirectSettings.setAccounts(new LinkedList<>());
        mainSettings.setBourseDirect(bourseDirectSettings);

        copyRulesTo(ezLoad.getRulesDir());

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

    private static void copyRulesTo(String rulesDir) throws IOException {
        InputStream stream = SettingsManager.class.getResourceAsStream("rules.zip");
        if (stream != null) {
            FileUtil.unzip(stream, rulesDir, false);
        }
    }
}
