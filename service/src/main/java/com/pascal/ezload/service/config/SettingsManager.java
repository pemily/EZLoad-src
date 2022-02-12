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
    public final static String RULE_SHARED_DIR =  "shared";
    public final static String RULE_LOCAL_DIR =  "local";
    private final static String CREDS_DIR = "creds";



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
        return settings;
    }

    private MainSettings readMainSettingsFile() throws Exception {
        if (new File(configFile).exists()) {
            try (Reader reader = new FileReader(configFile)) {
                return defaultValuesIfNotSet(yamlMapper.readValue(reader, MainSettings.class));
            }
        }
        else return defaultValuesIfNotSet(new MainSettings());
    }

    public void saveMainSettingsFile(MainSettings mainSettings) throws IOException {
        mainSettings.clearErrors();
        try(Writer writer = new FileWriter(configFile)) {
            yamlMapper.writeValue(writer, mainSettings);
        }
    }

    private EzProfil readEzProfilFile(String ezProfilName) throws Exception {
        try(Reader reader = new FileReader(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilName+"."+ezProfilFileExtension)) {
            return defaultValuesIfNotSet(ezProfilName, yamlMapper.readValue(reader, EzProfil.class), null);
        }
    }

    public void newEzProfil(String ezProfilName) throws Exception {
        MainSettings settings = readMainSettingsFile();
        EzProfil defaultProfil = readEzProfilFile(settings.getActiveEzProfilName());
        // comme le fichier de gdrive est pénible a faire, je le recupere du profil par defaut.
        createEzProfilIfNotExists(ezProfilName, defaultProfil.getEzPortfolio().getGdriveCredsFile());
    }

    public void saveEzProfilFile(String ezProfilName, EzProfil ezProfil) throws IOException {
        ezProfil.clearErrors();
        File outputFile = new File(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilName+"."+ezProfilFileExtension);
        try(Writer writer = new FileWriter(outputFile)) {
            yamlMapper.writeValue(writer, ezProfil);
        }
    }
    public void renameEzProfile(String oldProfilName, String newProfilName) throws Exception {
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

    public EzProfil getActiveEzProfil(MainSettings mainSettings) throws Exception {
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
        return new SettingsManager(searchConfigFilePath());
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

    public EzProfil createEzProfilIfNotExists(String ezProfilName, String initGDriveCredsFile) throws Exception {
        String ezProfilDirectory = getEzProfileDirectory(ezProfilName);

        if (StringUtils.isBlank(ezProfilName) || new File(ezProfilDirectory).exists()) return null;
        EzProfil ezProfil = null;
        return defaultValuesIfNotSet(ezProfilName, ezProfil, initGDriveCredsFile);
    }

    private String getEzProfileDirectory(String ezProfilName) {
        String ezHome = getEzHome();
        return ezHome+File.separator+profilesDirectory+File.separator+ ezProfilName +File.separator;
    }

    private EzProfil defaultValuesIfNotSet(String ezProfilName, EzProfil ezProfil, String initGDriveCredsFile) throws Exception {
        if (ezProfil == null) {
            ezProfil = new EzProfil();
        }
        String ezProfilDirectory = getEzProfileDirectory(ezProfilName);

        if (ezProfil.getCourtierCredsFile() == null) {
            ezProfil.setCourtierCredsFile(ezProfilDirectory + CREDS_DIR + File.separator + "ezCreds.json");
            try (FileOutputStream output = new FileOutputStream(ezProfil.getCourtierCredsFile())) {
                output.write("{}".getBytes(StandardCharsets.UTF_8));
            }
        }
        new File(ezProfil.getCourtierCredsFile()).getParentFile().mkdirs();

        if (ezProfil.getDownloadDir() == null){
            ezProfil.setDownloadDir(ezProfilDirectory+"courtiers");
        }
        new File(ezProfil.getDownloadDir()).mkdirs();

        ezProfil.setAnnualDividend(defaultValuesIfNotSet(ezProfil.getAnnualDividend()));
        ezProfil.setDividendCalendar(defaultValuesIfNotSet(ezProfil.getDividendCalendar()));

        ezProfil.setEzPortfolio(defaultValuesIfNotSet(ezProfilName, ezProfil.getEzPortfolio(), initGDriveCredsFile));
        ezProfil.setBourseDirect(defaultValuesIfNotSet(ezProfil.getBourseDirect()));

        saveEzProfilFile(ezProfilName, ezProfil);
        return ezProfil;
    }

    private BourseDirectSettings defaultValuesIfNotSet(BourseDirectSettings bourseDirectSettings) throws Exception {
        if (bourseDirectSettings == null){
            bourseDirectSettings = new BourseDirectSettings();
        }

        if (bourseDirectSettings.getAccounts() == null) {
            bourseDirectSettings.setAccounts(new LinkedList<>());
        }
        return bourseDirectSettings;
    }

    private EZPortfolioSettings defaultValuesIfNotSet(String ezProfilName, EZPortfolioSettings ezPortfolioSettings, String initGDriveCredsFile) throws Exception {
        if (ezPortfolioSettings == null){
            ezPortfolioSettings = new EZPortfolioSettings();
        }
        String ezProfilDirectory = getEzProfileDirectory(ezProfilName);
        if (ezPortfolioSettings.getEzPortfolioUrl() == null) ezPortfolioSettings.setEzPortfolioUrl(EZPORTFOLIO_GDRIVE_URL_PREFIX);
        if (ezPortfolioSettings.getGdriveCredsFile() == null){
            ezPortfolioSettings.setGdriveCredsFile(ezProfilDirectory+ CREDS_DIR +File.separator+"gdrive-access.json");
            new File(ezPortfolioSettings.getGdriveCredsFile()).getParentFile().mkdirs();
            if (initGDriveCredsFile == null) {
                FileUtil.string2file(ezPortfolioSettings.getGdriveCredsFile(), "{}");
            }
            else{
                String content = FileUtil.file2String(initGDriveCredsFile);
                FileUtil.string2file(ezPortfolioSettings.getGdriveCredsFile(), content == null ? "{}" : content);
            }

        }
        return ezPortfolioSettings;
    }

    private MainSettings defaultValuesIfNotSet(MainSettings mainSettings) throws Exception {
        mainSettings.setEzLoad(defaultValuesIfNotSet(mainSettings.getEzLoad()));
        mainSettings.setChrome(defaultValuesIfNotSet(mainSettings.getChrome()));
        if (StringUtils.isBlank(mainSettings.getActiveEzProfilName())) {
            mainSettings.setActiveEzProfilName(defaultEzProfilName);
        }

        createEzProfilIfNotExists(defaultEzProfilName, null);

        saveMainSettingsFile(mainSettings);
        return mainSettings;
    }

    private MainSettings.EZLoad defaultValuesIfNotSet(MainSettings.EZLoad ezLoad) throws Exception {
        if (ezLoad == null){
            ezLoad = new MainSettings.EZLoad();
        }
        String ezHome = getEzHome();
        if (ezLoad.getPort() == 0) ezLoad.setPort(2180);
        if (ezLoad.getLogsDir() == null) ezLoad.setLogsDir(ezHome+File.separator+"logs");
        if (ezLoad.getRulesDir() == null) ezLoad.setRulesDir(ezHome+File.separator+"rules");
        if (ezLoad.getPassPhrase() == null) ezLoad.setPassPhrase(AuthManager.getNewRandonmEncryptionPhrase()); // genString(42));

        new File(ezLoad.getRulesDir()).mkdirs();
        new File(ezLoad.getRulesDir()+File.separator+RULE_SHARED_DIR).mkdirs();
        new File(ezLoad.getRulesDir()+File.separator+RULE_LOCAL_DIR).mkdirs();
        new File(ezLoad.getLogsDir()).mkdirs();

        copyRulesTo(ezLoad.getRulesDir());

        ezLoad.setAdmin(defaultValuesIfNotSet(ezLoad.getAdmin()));

        return ezLoad;
    }

    private MainSettings.Admin defaultValuesIfNotSet(MainSettings.Admin admin) throws Exception {
        if (admin == null) {
            admin = new MainSettings.Admin();
            admin.setShowRules(false);
        }
        return admin;
    }

    private MainSettings.AnnualDividendConfig defaultValuesIfNotSet(MainSettings.AnnualDividendConfig annualDividendConfig) {
        if (annualDividendConfig == null) {
            annualDividendConfig = new MainSettings.AnnualDividendConfig();
        }
        if (annualDividendConfig.getDateSelector() == null)
            annualDividendConfig.setDateSelector(MainSettings.EnumAlgoDateSelector.DATE_DE_DETACHEMENT);
        if (annualDividendConfig.getYearSelector() == null)
            annualDividendConfig.setYearSelector(MainSettings.EnumAlgoYearSelector.ANNEE_PRECEDENTE);

        return annualDividendConfig;
    }

    private MainSettings.DividendCalendarConfig defaultValuesIfNotSet(MainSettings.DividendCalendarConfig dividendCalendarConfig) {
        if (dividendCalendarConfig == null) {
            dividendCalendarConfig = new MainSettings.DividendCalendarConfig();
        }

        if (dividendCalendarConfig.getDateSelector() == null)
            dividendCalendarConfig.setDateSelector(MainSettings.EnumAlgoDateSelector.DATE_DE_PAIEMENT);
        if (dividendCalendarConfig.getYearSelector() == null)
            dividendCalendarConfig.setYearSelector(MainSettings.EnumAlgoYearSelector.ANNEE_EN_COURS);
        if (dividendCalendarConfig.getPercentSelector() == null)
            dividendCalendarConfig.setPercentSelector(MainSettings.EnumPercentSelector.ADAPTATIF);
        return dividendCalendarConfig;
    }

    private MainSettings.ChromeSettings defaultValuesIfNotSet(MainSettings.ChromeSettings chromeSettings) throws Exception {
        if (chromeSettings == null) {
            chromeSettings = new MainSettings.ChromeSettings();
        }
        String ezHome = getEzHome();
        if (chromeSettings.getUserDataDir() == null)
            chromeSettings.setUserDataDir(ezHome + File.separator + "chrome" + File.separator + "data");
        // chromeDriver is a file that does not exists, so next time it will be downloaded
        if (chromeSettings.getDriverPath() == null)
            chromeSettings.setDriverPath(ezHome + File.separator + "chrome" + File.separator + "driver" + File.separator + "chromedriver");
        if (chromeSettings.getDefaultTimeout() == 0) chromeSettings.setDefaultTimeout(13);

        new File(chromeSettings.getUserDataDir()).mkdirs();
        new File(chromeSettings.getDriverPath()).getParentFile().mkdirs();
        return chromeSettings;
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
