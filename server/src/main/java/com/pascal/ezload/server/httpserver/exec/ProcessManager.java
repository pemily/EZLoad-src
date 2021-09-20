package com.pascal.ezload.server.httpserver.exec;

import com.pascal.ezload.service.config.MainSettings;
import jakarta.ws.rs.ext.Provider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Provider
public class ProcessManager {

    private List<EzProcess> ezProcesses = new ArrayList<>();

    public List<EzProcess> getProcesses() {
        return ezProcesses;
    }

    public void setProcesses(List<EzProcess> ezProcesses) {
        this.ezProcesses = ezProcesses;
    }

    public EzProcess getLatestProcess(){
        return ezProcesses.size() > 0 ? ezProcesses.get(ezProcesses.size()-1) : null;
    }

    public synchronized boolean createNewRunningProcess(String logFile){
        EzProcess latestProcess = getLatestProcess();
        if (latestProcess == null || !latestProcess.isRunning()){
            EzProcess p = new EzProcess(logFile);
            p.setRunning(true);
            ezProcesses.add(p);
            return true;
        }
        return false;
    }

    public static String getLog(MainSettings mainSettings, String prefix, String suffix){
        File logsDir = new File(mainSettings.getEZLoad().getLogsDir());
        Date now = new Date();
        String reportFileName =  prefix + new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(now) + suffix;
        return logsDir + File.separator + reportFileName;
    }

    public void kill() {
        EzProcess latestProcess = getLatestProcess();
        if (latestProcess != null && latestProcess.isRunning()){
            latestProcess.setRunning(false);
        }
    }
}
