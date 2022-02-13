package com.pascal.ezload.service.rules.update;

import com.pascal.ezload.github.handler.ApiException;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.FileUtil;
import com.pascal.ezload.service.util.HttpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownServiceException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RulesVersionManager {

    private static String owner = "pemily";
    private static String repo = "EZLoad-Rules";
    private static String ezLoadReleaseBranch = "EZLoad-1.0";

    Github ezLoadRelease = new Github(owner, repo, ezLoadReleaseBranch);

    // la doc officiel openapi de github: https://github.com/github/rest-api-description
    // https://github.com/github/rest-api-description/tree/main/descriptions-next/api.github.com

    private MainSettings mainSettings;

    public RulesVersionManager(MainSettings mainSettings){
        this.mainSettings = mainSettings;
    }

    public void synchSharedRulesFolder(Reporting reporting) throws ApiException, IOException {
        // BourseDirect first
        checkAndDownloadUpdates(reporting, EnumEZBroker.BourseDirect, 1);
    }

    private void checkAndDownloadUpdates(Reporting reporting, EnumEZBroker broker, int brokerFileVersion) throws ApiException, IOException {
        RulesManager rulesManager = new RulesManager(mainSettings);

        List<String> officialFilesFound = new LinkedList<>();

        try(Reporting rep = reporting.pushSection("Vérification des mises à jour des règles de "+broker.getEzPortfolioName()+" v"+brokerFileVersion)) {
            ezLoadRelease.getOfficialFiles(broker, brokerFileVersion)
                    .forEach(remoteFile -> {
                        officialFilesFound.add(remoteFile.getFilename());
                        File downloadingFile = new File(rulesManager.getFile(remoteFile.getFilename(), broker, brokerFileVersion, false)+".downloading");
                        try {
                            HttpUtil.download(remoteFile.getDownloadUrl(), downloadingFile);

                            File currentFile = new File(rulesManager.getFile(remoteFile.getFilename(), broker, brokerFileVersion, false));
                            if (!currentFile.exists()){
                                rep.info(broker.getEzPortfolioName()+" v"+brokerFileVersion+" - Nouvelle règle: "+remoteFile.getFilename());
                                downloadingFile.renameTo(currentFile);
                            }
                            else{
                                String currentHash = FileUtil.hashCode(currentFile);
                                String newHash = FileUtil.hashCode(downloadingFile);

                                if (!currentHash.equals(newHash)){
                                    rep.info(broker.getEzPortfolioName()+" v"+brokerFileVersion+" - Mise à jour de la règle: "+remoteFile.getFilename());
                                    currentFile.delete();
                                    downloadingFile.renameTo(currentFile);
                                }
                            }
                            downloadingFile.delete();
                        } catch (Exception e) {
                            throw new RuntimeException("Erreur pendant la mise à jour de la règle: "+remoteFile.getFilename()+" pour "+broker.getEzPortfolioName()+" v"+brokerFileVersion);
                        }
                    });

            // suppressions des fichiers locaux non détecté sur le remote
            File rulesDir = new File(rulesManager.getRulesDirectory(broker, brokerFileVersion, false));
            File[] existingRules = rulesDir.listFiles();
            if (existingRules != null)
                Arrays.stream(existingRules).filter(f -> !officialFilesFound.contains(f.getName())).forEach(File::delete);

            reporting.info("Vous êtes à jour");
        }
    }
}
