package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.server.httpserver.exec.EzProcess;

public class WebData {

    private MainSettings mainSettings;
    private EzProcess latestEzProcess;

    public WebData(MainSettings mainSettings, EzProcess latestEzProcess){
        this.mainSettings = mainSettings;
        this.latestEzProcess = latestEzProcess;
    }

    public MainSettings getMainSettings() {
        return mainSettings;
    }

    public void setMainSettings(MainSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public EzProcess getLatestProcess() {
        return latestEzProcess;
    }

    public void setLatestProcess(EzProcess latestEzProcess) {
        this.latestEzProcess = latestEzProcess;
    }
}
