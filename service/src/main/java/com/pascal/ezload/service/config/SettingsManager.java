package com.pascal.ezload.service.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.security.AuthManager;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.util.FileProcessor;
import com.pascal.ezload.service.util.FileUtil;
import org.apache.commons.lang3.StringUtils;



public class SettingsManager {
    private static final Logger logger = Logger.getLogger("SettingsManager");

    private final String configFile;
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final String ezProfilFileExtension = "ezl";
    private static final String defaultEzProfilFilename = "defaut."+ ezProfilFileExtension;
    private static final String profilesDirectory = "profiles";
    public final static String EZPORTFOLIO_GDRIVE_URL_PREFIX = "https://docs.google.com/spreadsheets/d/";

    private SettingsManager(String configFile){
        this.configFile = configFile;
    }

    public MainSettings loadProps() throws Exception {
        MainSettings settings = readMainSettingsFile();
        String passphrase = settings.getEzLoad().getPassPhrase();
        if (passphrase == null){
            settings.getEzLoad().setPassPhrase(AuthManager.getNewRandonmEncryptionPhrase());
            saveMainSettingsFile(settings);
        }
        return settings;
    }

    private MainSettings readMainSettingsFile() throws IOException {
        try(Reader reader = new FileReader(configFile)) {
            return yamlMapper.readValue(reader, MainSettings.class);
        }
    }

    public void saveMainSettingsFile(MainSettings mainSettings) throws IOException {
        mainSettings.clearErrors();
        try(Writer writer = new FileWriter(configFile)) {
            yamlMapper.writeValue(writer, mainSettings);
        }
    }

    private EzProfil readEzProfilFile(String ezProfilFilename) throws IOException {
        try(Reader reader = new FileReader(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilFilename)) {
            return yamlMapper.readValue(reader, EzProfil.class);
        }
    }

    public void saveEzProfilFile(String ezProfilFilename, EzProfil ezProfil) throws IOException {
        ezProfil.clearErrors();
        try(Writer writer = new FileWriter(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilFilename)) {
            yamlMapper.writeValue(writer, ezProfil);
        }
    }

    public EzProfil getActiveEzProfil(MainSettings mainSettings) throws IOException {
        String ezProfilFilename = StringUtils.isBlank(mainSettings.getActiveEzProfilFilename()) ? defaultEzProfilFilename : mainSettings.getActiveEzProfilFilename();
        return readEzProfilFile(ezProfilFilename);
    }


    public List<String> listAllEzProfiles() throws IOException {
        return new FileProcessor(getEzHome(), d -> false, f -> f.getName().endsWith("."+ ezProfilFileExtension))
                .mapFile(f -> new File(f).getName());
    }

    public static AuthManager getAuthManager(MainSettings mainSettings, EzProfil ezProfil) {
        return new AuthManager(mainSettings.getEzLoad().getPassPhrase(), ezProfil.getCourtierCredsFile());
    }

    public String getEzHome() {
        return new File(configFile).getParentFile().getAbsolutePath();
    }

    public static String searchConfigFilePath(){
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
        String configFilePath = searchConfigFilePath();
        if (new File(configFilePath).exists()){
            return new SettingsManager(configFilePath);
        }
        SettingsManager settingsManager = new SettingsManager(configFilePath);
        settingsManager.createInitialSettings();
        return settingsManager;
    }

    public Consumer<String> saveNewChromeDriver(){
        return newChromeDriver -> {
            try {
                MainSettings mainSettings = this.loadProps();
                mainSettings.getChrome().setDriverPath(newChromeDriver);
                this.saveMainSettingsFile(mainSettings);
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
        };
    }

    public EzProfil createEzProfil(String ezProfilFilename) throws IOException {
        String ezHome = getEzHome();

        EzProfil ezProfil = new EzProfil();
        String credsDir = "creds";
        String ezProfilDirectory = ezHome+File.separator+profilesDirectory+File.separator+ezProfilFilename.substring(0, ezProfilFilename.length()- ezProfilFileExtension.length()-1)+File.separator;

        ezProfil.setCourtierCredsFile(ezProfilDirectory+credsDir+File.separator+"ezCreds.json");
        new File(ezProfil.getCourtierCredsFile()).getParentFile().mkdirs();

        try (FileOutputStream output = new FileOutputStream(ezProfil.getCourtierCredsFile())) {
            output.write("{}".getBytes(StandardCharsets.UTF_8));
        }

        EZPortfolioSettings ezPortfolioSettings = new EZPortfolioSettings();
        ezProfil.setEzPortfolio(ezPortfolioSettings);
        ezPortfolioSettings.setEzPortfolioUrl(EZPORTFOLIO_GDRIVE_URL_PREFIX);
        ezPortfolioSettings.setGdriveCredsFile(ezProfilDirectory+credsDir+File.separator+"gdrive-access.json");
        new File(ezPortfolioSettings.getGdriveCredsFile()).getParentFile().mkdirs();
        try (FileOutputStream output = new FileOutputStream(ezPortfolioSettings.getGdriveCredsFile())) {
            output.write("{}".getBytes(StandardCharsets.UTF_8));
        }

        BourseDirectSettings bourseDirectSettings = new BourseDirectSettings();
        bourseDirectSettings.setAccounts(new LinkedList<>());
        ezProfil.setBourseDirect(bourseDirectSettings);

        ezProfil.setDownloadDir(ezProfilDirectory+"courtiers");
        new File(ezProfil.getDownloadDir()).mkdirs();

        saveEzProfilFile(ezProfilFilename, ezProfil);
        return ezProfil;
    }

    private MainSettings createInitialSettings() throws IOException {
        MainSettings mainSettings = new MainSettings();
        MainSettings.EZLoad ezLoad = new MainSettings.EZLoad();
        String ezHome = getEzHome();
        MainSettings.Admin admin = new MainSettings.Admin();
        admin.setShowRules(false);
        ezLoad.setPort(2180);
        ezLoad.setAdmin(admin);
        ezLoad.setLogsDir(ezHome+File.separator+"logs");
        ezLoad.setRulesDir(ezHome+File.separator+"rules");
        ezLoad.setPassPhrase(genString(42));

        new File(ezLoad.getRulesDir()).mkdirs();
        new File(ezLoad.getLogsDir()).mkdirs();
        mainSettings.setEzLoad(ezLoad);

        MainSettings.ChromeSettings chromeSettings = new MainSettings.ChromeSettings();
        chromeSettings.setUserDataDir(ezHome+File.separator+"chrome"+File.separator+"data");
        // chromeDriver is a file that does not exists, so next time it will be downloaded
        chromeSettings.setDriverPath(ezHome+File.separator+"chrome"+File.separator+"driver"+File.separator+"chromedriver");
        chromeSettings.setDefaultTimeout(5);
        new File(chromeSettings.getUserDataDir()).mkdirs();
        new File(chromeSettings.getDriverPath()).getParentFile().mkdirs();
        mainSettings.setChrome(chromeSettings);

        copyRulesTo(ezLoad.getRulesDir());

        mainSettings.setActiveEzProfilFilename(defaultEzProfilFilename);
        createEzProfil(defaultEzProfilFilename);
        saveMainSettingsFile(mainSettings);

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
        InputStream stream = SettingsManager.class.getClassLoader().getResourceAsStream("rules.zip");
        if (stream == null){
            stream = new FileInputStream("service/target/rules.zip");
        }
        FileUtil.unzip(stream, rulesDir, false);
    }

    public static String getVersion() throws IOException {
        final Properties properties = new Properties();
        properties.load(SettingsManager.class.getClassLoader().getResourceAsStream("about.properties"));
        return properties.getProperty("version");
    }

    public static String getDownloadDir(EzProfil ezProfil, EnumEZBroker brCourtier) {
        return ezProfil.getDownloadDir()+ File.separator+brCourtier.getDirName();
    }
}
