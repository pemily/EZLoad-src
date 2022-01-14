package com.pascal.ezload.service.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.security.AuthManager;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;



public class SettingsManager {
    private static final Logger logger = Logger.getLogger("SettingsManager");

    private final String configFile;
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final String ezProfilFileExtension = "ezl";
    private static final String defaultEzProfilName = getDefaultProfileName();
    private static final String profilesDirectory = "profiles";
    public final static String EZPORTFOLIO_GDRIVE_URL_PREFIX = "https://docs.google.com/spreadsheets/d/";


    private SettingsManager(String configFile){
        this.configFile = configFile;
    }

    private static String getDefaultProfileName(){
        if (StringUtils.isBlank(System.getProperty("user.name"))){
            return "Defaut";
        }
        return System.getProperty("user.name");
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

    private EzProfil readEzProfilFile(String ezProfilName) throws IOException {
        try(Reader reader = new FileReader(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilName+"."+ezProfilFileExtension)) {
            return yamlMapper.readValue(reader, EzProfil.class);
        }
    }

    public void newEzProfil(String ezProfilName) throws IOException {
        MainSettings settings = readMainSettingsFile();
        EzProfil defaultProfil = readEzProfilFile(settings.getActiveEzProfilName());
        // comme le fichier de gdrive est pénible a faire, je le recupere du profil par defaut.
        createEzProfil(ezProfilName, defaultProfil.getEzPortfolio().getGdriveCredsFile());
    }

    public void saveEzProfilFile(String ezProfilName, EzProfil ezProfil) throws IOException {
        ezProfil.clearErrors();
        File outputFile = new File(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilName+"."+ezProfilFileExtension);
        try(Writer writer = new FileWriter(outputFile)) {
            yamlMapper.writeValue(writer, ezProfil);
        }
    }
    public void renameEzProfile(String oldProfilName, String newProfilName) throws IOException {
        String oldProfileDir = getEzProfileDirectory(oldProfilName);
        String newProfileDir = getEzProfileDirectory(newProfilName);

        if (StringUtils.isBlank(newProfilName) || new File(newProfileDir).exists()) return;

        FileUtils.moveDirectory(new File(oldProfileDir), new File(newProfileDir));
        FileUtils.moveFile(new File(getEzHome()+File.separator+profilesDirectory+File.separator+oldProfilName+"."+ezProfilFileExtension),
                new File(getEzHome()+File.separator+profilesDirectory+File.separator+newProfilName+"."+ezProfilFileExtension));

        MainSettings settings = readMainSettingsFile();
        if (settings.getActiveEzProfilName().equals(oldProfilName)){
            settings.setActiveEzProfilName(newProfilName);
            saveMainSettingsFile(settings);
        }

        EzProfil newEzProfil = readEzProfilFile(newProfilName);
        if (newEzProfil.getDownloadDir().startsWith(oldProfileDir)){
            newEzProfil.setDownloadDir(newEzProfil.getDownloadDir().replace(oldProfileDir, newProfileDir));
        }
        if (newEzProfil.getCourtierCredsFile().startsWith(oldProfileDir)){
            newEzProfil.setCourtierCredsFile(newEzProfil.getCourtierCredsFile().replace(oldProfileDir, newProfileDir));
        }
        if (newEzProfil.getEzPortfolio().getGdriveCredsFile().startsWith(oldProfileDir)){
            newEzProfil.getEzPortfolio().setGdriveCredsFile(newEzProfil.getEzPortfolio().getGdriveCredsFile().replace(oldProfileDir, newProfileDir));
        }
        saveEzProfilFile(newProfilName, newEzProfil);
    }

    public void deleteEzProfil(String ezProfilName) throws IOException {
        File profilDir = new File(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilName);
        FileUtil.rmdir(profilDir);
        new File(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilName+"."+ezProfilFileExtension).delete();
    }

    public EzProfil getActiveEzProfil(MainSettings mainSettings) throws IOException {
        String ezProfilName = StringUtils.isBlank(mainSettings.getActiveEzProfilName()) ? defaultEzProfilName : mainSettings.getActiveEzProfilName();
        return readEzProfilFile(ezProfilName);
    }


    public List<String> listAllEzProfiles() {
        String profiles[] = new File(getEzHome()+File.separator+profilesDirectory)
                .list((f, name) -> name.endsWith("."+ ezProfilFileExtension));
        if (profiles == null) return new ArrayList<>();
        return Arrays.asList(profiles).stream()
                .map(s -> s.substring(0, s.length()-(ezProfilFileExtension.length()+1))) // remove the extension
                .collect(Collectors.toList());
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

    public EzProfil createEzProfil(String ezProfilName, String initGDriveCredsFile) throws IOException {
        String ezProfilDirectory = getEzProfileDirectory(ezProfilName);

        if (StringUtils.isBlank(ezProfilName) || new File(ezProfilDirectory).exists()) return null;

        EzProfil ezProfil = new EzProfil();
        String credsDir = "creds";

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
        if (initGDriveCredsFile == null) {
            FileUtil.string2file(ezPortfolioSettings.getGdriveCredsFile(), "{}");
        }
        else{
            String content = FileUtil.file2String(initGDriveCredsFile);
            if (content != null) FileUtil.string2file(ezPortfolioSettings.getGdriveCredsFile(), content);
        }

        BourseDirectSettings bourseDirectSettings = new BourseDirectSettings();
        bourseDirectSettings.setAccounts(new LinkedList<>());
        ezProfil.setBourseDirect(bourseDirectSettings);

        ezProfil.setDownloadDir(ezProfilDirectory+"courtiers");
        new File(ezProfil.getDownloadDir()).mkdirs();

        saveEzProfilFile(ezProfilName, ezProfil);
        return ezProfil;
    }

    private String getEzProfileDirectory(String ezProfilName) {
        String ezHome = getEzHome();
        String ezProfilDirectory = ezHome+File.separator+profilesDirectory+File.separator+ ezProfilName +File.separator;
        return ezProfilDirectory;
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
        chromeSettings.setDefaultTimeout(10);
        new File(chromeSettings.getUserDataDir()).mkdirs();
        new File(chromeSettings.getDriverPath()).getParentFile().mkdirs();
        mainSettings.setChrome(chromeSettings);

        copyRulesTo(ezLoad.getRulesDir());

        mainSettings.setActiveEzProfilName(defaultEzProfilName);
        createEzProfil(defaultEzProfilName, null);
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
