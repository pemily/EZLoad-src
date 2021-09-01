package com.pascal.ezload.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.model.EnumBRCourtier;
import com.pascal.ezload.service.security.AuthManager;

import java.io.*;

public class SettingsManager {
    private final String configFile;

    public SettingsManager(String configFile){
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

    public static AuthManager getAuthManager(MainSettings mainSettings){
        return new AuthManager(mainSettings.getEZLoad().getPassPhrase(), mainSettings.getEZLoad().getCourtierCredsFile());
    }

    public static String getDownloadDir(MainSettings mainSettings, EnumBRCourtier brCourtier){
        return mainSettings.getEZLoad().getDownloadDir()+ File.separator+brCourtier.getDirName();
    }
}
