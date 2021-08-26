package com.pascal.bientotrentier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.bientotrentier.service.config.MainSettings;
import com.pascal.bientotrentier.service.security.AuthManager;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class SettingsManager {
    private final String configFile;

    public SettingsManager(String configFile){
        this.configFile = configFile;
    }

    public MainSettings loadProps() throws Exception {
        MainSettings settings = readConfigFile();
        String passphrase = settings.getBientotRentier().getPassPhrase();
        if (passphrase == null){
            settings.getBientotRentier().setPassPhrase(AuthManager.getNewRandonmEncryptionPhrase());
            saveConfigFile(settings);
        }
        return settings;
    }

    public static AuthManager getAuthManager(MainSettings mainSettings){
        return new AuthManager(mainSettings.getBientotRentier().getPassPhrase(), mainSettings.getBientotRentier().getCourtierCredsFile());
    }

    private MainSettings readConfigFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try(Reader reader = new FileReader(configFile)) {
             return mapper.readValue(reader, MainSettings.class);
        }
    }

    private void saveConfigFile(MainSettings mainSettings) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new FileWriter(configFile), mainSettings);
    }

}
