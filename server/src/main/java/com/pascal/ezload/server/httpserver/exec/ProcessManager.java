package com.pascal.ezload.server.httpserver.exec;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.util.Tail;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.Provider;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessManager {

    private final EZHttpServer server;

    public ProcessManager(EZHttpServer server){
        this.server = server;
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

    public synchronized EzProcess createNewRunningProcess(MainSettings mainSettings, String title, String logFile, RunnableWithException runnable) throws IOException {
        EzProcess latestProcess = getLatestProcess();
        if (latestProcess == null || !latestProcess.isRunning()){
            EzProcess p = new EzProcess(title, logFile);
            p.setRunning(true);
            ezProcesses.add(p);
            Writer fileWriter = new BufferedWriter(new FileWriter(logFile));

            executor.submit(() -> {
                try (HttpProcessRunner processLogger = new HttpProcessRunner(p, fileWriter, server.fileLinkCreator(mainSettings))) {
                    try {
                        processLogger.header(processLogger.getReporting().escape(title));
                        runnable.run(processLogger);
                    } catch (Exception e) {
                        processLogger.getReporting().error(e);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally{
                    p.setRunning(false);
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
            htmlPageWriter.close();
            return;
        }
        File logFile = new File(latestProcess.getLogFile());
        if ((!logFile.exists() && !logFile.isFile())){
            htmlPageWriter.close();
            return;
        }
        if (latestProcess.isRunning()) {
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
            }

        }
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
            executor.shutdownNow();
            latestProcess.setRunning(false);
        }
    }
}
