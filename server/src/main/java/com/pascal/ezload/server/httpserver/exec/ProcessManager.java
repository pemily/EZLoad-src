/**
 * ezServer - EZLoad an automatic loader for EZPortfolio
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
package com.pascal.ezload.server.httpserver.exec;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.util.Tail;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessManager {

    private final EZHttpServer server;
    private final EzServerState serverState;

    public ProcessManager(EZHttpServer server, EzServerState serverState){
        this.server = server;
        this.serverState = serverState;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
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

    public interface RunnableWithException {
        void run(HttpProcessRunner processRunner) throws Exception;
    }

    public synchronized EzProcess createNewRunningProcess(SettingsManager settingsManager, MainSettings mainSettings, String title, String logFile, RunnableWithException runnable) throws IOException {
        EzProcess latestProcess = getLatestProcess();
        if (latestProcess == null || !serverState.isProcessRunning()){
            EzProcess p = new EzProcess(title, logFile);
            Writer fileWriter = new FileWriter(logFile);
            serverState.setProcessRunning(true);
            ezProcesses.add(p);

            executor.submit(() -> {
                try (HttpProcessRunner processLogger = new HttpProcessRunner(fileWriter, server.fileLinkCreator(settingsManager, mainSettings))) {
                    try {
                        processLogger.header(processLogger.getReporting().escape(title));
                        runnable.run(processLogger);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        processLogger.getReporting().error(e);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally{
                    serverState.setProcessRunning(false);
                }
            });
            return p;
        }
        return null;
    }

    // the log can be closed or running
    public void viewLogProcess(Writer htmlPageWriter) throws IOException {
        EzProcess latestProcess = getLatestProcess();
        if (latestProcess == null){
            return;
        }
        File logFile = new File(latestProcess.getLogFile());
        if ((!logFile.exists() && !logFile.isFile())){
            return;
        }
        if (serverState.isProcessRunning()) {
            Tail.tail(logFile, htmlPageWriter, HttpProcessRunner.FILE_HEADER, HttpProcessRunner.FILE_FOOTER);
        }
        else{
            BufferedReader logReader = new BufferedReader(new FileReader(logFile));
            boolean isFileHeaderRead = false;
            for(String line = logReader.readLine(); line != null; line = logReader.readLine()) {
                if (!isFileHeaderRead){
                    if (line.equals(HttpProcessRunner.FILE_HEADER)) isFileHeaderRead = true;
                    continue;
                }
                if (line.equals(HttpProcessRunner.FILE_FOOTER)) break;
                htmlPageWriter.write(line);
                htmlPageWriter.flush();
            }
        }
    }

    public static String getLog(MainSettings mainSettings, String prefix, String suffix){
        File logsDir = new File(mainSettings.getEzLoad().getLogsDir());
        Date now = new Date();
        String reportFileName =  new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(now) + "-" + prefix + suffix;
        return logsDir + File.separator + reportFileName;
    }

    public void kill() {
        EzProcess latestProcess = getLatestProcess();
        if (latestProcess != null && serverState.isProcessRunning()){
            executor.shutdownNow();
            serverState.setProcessRunning(false);
        }
    }
}
