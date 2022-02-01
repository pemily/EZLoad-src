package com.pascal.ezload.service.rules.update;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;

public class RulesVersionManager {

    private static String remoteSharedBranchName = "EZLoad-1.0";

    // la doc officiel openapi de github: https://github.com/github/rest-api-description
    // https://github.com/github/rest-api-description/tree/main/descriptions-next/api.github.com

    private MainSettings mainSettings;

    public RulesVersionManager(MainSettings mainSettings){
        this.mainSettings = mainSettings;
    }

    public void synchSharedRulesFolder(){
        mainSettings.getEzLoad().getRulesDir();

    }
}
