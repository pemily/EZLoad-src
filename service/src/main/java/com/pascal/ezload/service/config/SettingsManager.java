/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.security.AuthManager;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.common.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;



public class SettingsManager {
    private static final Logger logger = Logger.getLogger("SettingsManager");
    public static final String EZLOAD_CONFIG_YAML = "ezload-config.yaml";
    public static final String GDRIVE_ACCESS_JSON_FILE = "gdrive-access.json";

    private final String configFile;
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final String ezProfilFileExtension = "ezl";
    private static final String defaultEzProfilName = getDefaultProfileName();
    private static final String profilesDirectory = "profiles";
    public final static String EZPORTFOLIO_GDRIVE_URL_PREFIX = "https://docs.google.com/spreadsheets/d/";
    private final static String CREDS_DIR = "creds";
    private static final String DOWNLOAD_DIR ="courtiers";
    private static final String COURTIER_CREDS_FILE = "ezCreds.json";



    public SettingsManager(String configFile){
        this.configFile = configFile;
    }

    public String getConfigFile(){
        return configFile;
    }

    private static String getDefaultProfileName(){
        if (StringUtils.isBlank(System.getProperty("user.name"))){
            return "Défaut";
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
        if (!new File(configFile).exists()) {
            new File(configFile).getParentFile().mkdirs();
        }
        try(Writer writer = new FileWriter(configFile)) {
            yamlMapper.writeValue(writer, mainSettings);
        }
    }

    public EzProfil readEzProfilFile(String ezProfilName) throws Exception {
        try(Reader reader = new FileReader(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilName+"."+ezProfilFileExtension)) {
            return defaultValuesIfNotSet(ezProfilName, yamlMapper.readValue(reader, EzProfil.class), null);
        }
    }

    public void newEzProfil(String ezProfilName) throws Exception {
        MainSettings settings = readMainSettingsFile();
        String defaultProfilName = settings.getActiveEzProfilName();
        // comme le fichier de gdrive est pénible a faire, je le recupere du profil par defaut.
        String defaultGDriveFile = getGDriveCredsFile(defaultProfilName);
        createEzProfilIfNotExists(ezProfilName, defaultGDriveFile);
    }

    public String getGDriveCredsFile(String defaultProfilName) {
        return getEzProfilDir(defaultProfilName, CREDS_DIR) + File.separator + GDRIVE_ACCESS_JSON_FILE;
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
        // read the settings before the rename
        MainSettings settings = readMainSettingsFile();
        EzProfil newEzProfil = readEzProfilFile(oldProfilName);

        if (StringUtils.isBlank(newProfilName) || new File(newProfileDir).exists()) return;

        // move the directory & files
        FileUtils.moveDirectory(new File(oldProfileDir), new File(newProfileDir));
        FileUtils.moveFile(new File(getEzHome()+File.separator+profilesDirectory+File.separator+oldProfilName+"."+ezProfilFileExtension),
                new File(getEzHome()+File.separator+profilesDirectory+File.separator+newProfilName+"."+ezProfilFileExtension));

        // update the settings
        if (settings.getActiveEzProfilName().equals(oldProfilName)){
            settings.setActiveEzProfilName(newProfilName);
            saveMainSettingsFile(settings);
        }

        saveEzProfilFile(newProfilName, newEzProfil);
    }

    public void deleteEzProfil(String ezProfilName) throws IOException {
        File profilDir = new File(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilName);
        FileUtil.rmdir(profilDir);
        new File(getEzHome()+File.separator+profilesDirectory+File.separator+ezProfilName+"."+ezProfilFileExtension).delete();
    }

    public static String getActiveEzProfileName(MainSettings mainSettings){
        return StringUtils.isBlank(mainSettings.getActiveEzProfilName()) ? defaultEzProfilName : mainSettings.getActiveEzProfilName();
    }

    public EzProfil getActiveEzProfil(MainSettings mainSettings) throws Exception {
        return readEzProfilFile(getActiveEzProfileName(mainSettings));
    }


    public List<String> listAllEzProfiles() {
        String profiles[] = new File(getEzHome()+File.separator+profilesDirectory)
                .list((f, name) -> name.endsWith("."+ ezProfilFileExtension));
        if (profiles == null) return new ArrayList<>();
        return Arrays.asList(profiles).stream()
                .map(s -> s.substring(0, s.length()-(ezProfilFileExtension.length()+1))) // remove the extension
                .collect(Collectors.toList());
    }

    public AuthManager getAuthManager(MainSettings mainSettings) {
        return new AuthManager(mainSettings.getEzLoad().getPassPhrase(), getCredsDir(mainSettings.getActiveEzProfilName())+ File.separator + COURTIER_CREDS_FILE);
    }

    private String getCredsDir(String ezProfilName) {
        return getEzProfilDir(ezProfilName, CREDS_DIR);
    }


    public String getEzHome() {
        return new File(configFile).getParentFile().getAbsolutePath();
    }

    public String getEzLoadRepoDir() {
        return getEzHome()+File.separator+"repo";
    }

    private static String getRedirectFile(){
        String userDir = System.getProperty("user.home");
        return userDir + File.separator + "ezload.txt";
    }

    public void moveDone() throws IOException {
        FileUtil.string2file(getRedirectFile(), configFile);
    }

    public static String searchConfigFilePath() throws IOException {
        String configFile = System.getProperty("ezloadConfig");
        if (configFile == null) configFile = System.getenv("ezloadConfig");

        if (configFile == null){
            String dirRedirect = getRedirectFile();
            if (new File(dirRedirect).exists()) {
                configFile = FileUtil.file2String(dirRedirect);
            }
            else {
                String userDir = System.getProperty("user.home");
                configFile = userDir + File.separator + "EZLoad" + File.separator + EZLOAD_CONFIG_YAML;
                FileUtil.string2file(dirRedirect, configFile);
            }
        }

        return configFile;
    }

    public static SettingsManager getInstance() throws IOException {
        return new SettingsManager(searchConfigFilePath());
    }

    public EzProfil createEzProfilIfNotExists(String ezProfilName, String initGDriveCredsFile) throws Exception {
        String ezProfilDirectory = getEzProfileDirectory(ezProfilName);

        if (StringUtils.isBlank(ezProfilName) || new File(ezProfilDirectory).exists()) return null;
        EzProfil ezProfil = null;
        return defaultValuesIfNotSet(ezProfilName, ezProfil, initGDriveCredsFile);
    }

    public String getEzProfileDirectory(String ezProfilName) {
        String ezHome = getEzHome();
        return ezHome+File.separator+profilesDirectory+File.separator+ ezProfilName;
    }

    private EzProfil defaultValuesIfNotSet(String ezProfilName, EzProfil ezProfil, String initGDriveCredsFile) throws Exception {
        if (ezProfil == null) {
            ezProfil = new EzProfil();
        }
        String ezProfilDirectory = getEzProfileDirectory(ezProfilName);
        new File(ezProfilDirectory).mkdirs();

        String credsCourtierFile = getCredsDir(ezProfilName)+ File.separator + COURTIER_CREDS_FILE;
        if (!new File(credsCourtierFile).exists()) {
            new File(credsCourtierFile).getParentFile().mkdirs();
            try (FileOutputStream output = new FileOutputStream(credsCourtierFile)) {
                output.write("{}".getBytes(StandardCharsets.UTF_8));
            }
        }

        new File(getEzProfilDir(ezProfilName, DOWNLOAD_DIR)).mkdirs();

        ezProfil.setAnnualDividend(defaultValuesIfNotSet(ezProfil.getAnnualDividend()));
        ezProfil.setDividendCalendar(defaultValuesIfNotSet(ezProfil.getDividendCalendar()));

        ezProfil.setEzPortfolio(defaultValuesIfNotSet(ezProfilName, ezProfil.getEzPortfolio(), initGDriveCredsFile));
        ezProfil.setBourseDirect(defaultValuesIfNotSet(ezProfil.getBourseDirect()));

        saveEzProfilFile(ezProfilName, ezProfil);
        return ezProfil;
    }

    private BourseDirectSettings defaultValuesIfNotSet(BourseDirectSettings bourseDirectSettings) {
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

        if (ezPortfolioSettings.getEzPortfolioUrl() == null) ezPortfolioSettings.setEzPortfolioUrl(EZPORTFOLIO_GDRIVE_URL_PREFIX);

        String gdriveCredsFile = getGDriveCredsFile(ezProfilName);
        if (!new File(gdriveCredsFile).exists()) {
            new File(gdriveCredsFile).getParentFile().mkdirs();
            if (initGDriveCredsFile == null) {
                FileUtil.string2file(gdriveCredsFile, "{}");
            } else {
                String content = FileUtil.file2String(initGDriveCredsFile);
                FileUtil.string2file(gdriveCredsFile, content == null ? "{}" : content);
            }
        }

        return ezPortfolioSettings;
    }

    private MainSettings defaultValuesIfNotSet(MainSettings mainSettings) throws Exception {
        mainSettings.setEzLoad(defaultValuesIfNotSet(mainSettings.getEzLoad()));
        mainSettings.setChrome(defaultValuesIfNotSet(mainSettings.getChrome()));
        String ezProfilName = defaultEzProfilName;
        if (StringUtils.isBlank(mainSettings.getActiveEzProfilName())) {
            mainSettings.setActiveEzProfilName(ezProfilName);
        }
        else{
            ezProfilName = mainSettings.getActiveEzProfilName();
        }

        createEzProfilIfNotExists(ezProfilName, null);

        saveMainSettingsFile(mainSettings);
        
        return mainSettings;
    }

    private MainSettings.EZLoad defaultValuesIfNotSet(MainSettings.EZLoad ezLoad) throws Exception {
        if (ezLoad == null){
            ezLoad = new MainSettings.EZLoad();
        }

        if (ezLoad.getPort() == 0) ezLoad.setPort(2180);
        if (ezLoad.getLogsDir() == null) ezLoad.setLogsDir(Files.createTempDirectory("ezLoad").toFile().getAbsolutePath()+File.separator+"logs");
        if (ezLoad.getPassPhrase() == null) ezLoad.setPassPhrase(AuthManager.getNewRandonmEncryptionPhrase()); // genString(42));
        if (ezLoad.getCacheDir() == null) ezLoad.setCacheDir(Files.createTempDirectory("ezLoad").toFile().getAbsolutePath()+File.separator+"cache");

        new File(ezLoad.getLogsDir()).mkdirs();
        new File(ezLoad.getCacheDir()).mkdirs();

        ezLoad.setAdmin(defaultValuesIfNotSet(ezLoad.getAdmin()));

        return ezLoad;
    }

    private MainSettings.Admin defaultValuesIfNotSet(MainSettings.Admin admin) {
        if (admin == null) {
            admin = new MainSettings.Admin();
            admin.setShowRules(false);
        }
        if (admin.getBranchName() == null)
            admin.setBranchName(getDefaultProfileName().replaceAll(" ", ""));
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
            dividendCalendarConfig.setPercentSelector(MainSettings.EnumPercentSelector.STABLE);
        return dividendCalendarConfig;
    }

    private MainSettings.ChromeSettings defaultValuesIfNotSet(MainSettings.ChromeSettings chromeSettings) throws Exception {
        if (chromeSettings == null) {
            chromeSettings = new MainSettings.ChromeSettings();
        }
        if (chromeSettings.getUserDataDir() == null)
            chromeSettings.setUserDataDir(Files.createTempDirectory("ezload").toFile().getAbsolutePath()+File.separator+"chromeData");

        if (chromeSettings.getDefaultTimeout() == 0) chromeSettings.setDefaultTimeout(20);

        new File(chromeSettings.getUserDataDir()).mkdirs();

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

    public static String getVersion() throws IOException {
        final Properties properties = new Properties();
        properties.load(SettingsManager.class.getClassLoader().getResourceAsStream("about.properties"));
        return properties.getProperty("version");
    }

    public String getDownloadDir(String defaultEzProfilName, EnumEZBroker brCourtier) {
        String dirStr = getEzProfilDir(defaultEzProfilName, DOWNLOAD_DIR) + File.separator + brCourtier.getDirName();
        File dir = new File(dirStr);
        if (!dir.exists()) dir.mkdirs();
        return dirStr;
    }

    public String getDir(String subdir) {
        return getEzHome()+File.separator+subdir;
    }


    public String getEzProfilDir(String ezProfilName, String subdir) {
        return getEzProfileDirectory(ezProfilName)+File.separator+subdir;
    }

    public String getShareDataFile() {
        return getDir("shareData.json");
    }

    public String getDashboardFile() {
        return getDir("dashboard.json");
    }

    public String getRulesDir() {
        return getDir("repo"+ File.separator + "rules");
    }
}
