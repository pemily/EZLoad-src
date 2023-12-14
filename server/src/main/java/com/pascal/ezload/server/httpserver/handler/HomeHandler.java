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
package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.WebData;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.financial.ActionWithMsg;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.rules.update.RulesVersionManager;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectSearchAccounts;
import com.pascal.ezload.service.util.FileUtil;
import com.pascal.ezload.service.util.LoggerReporting;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("home")
public class HomeHandler {
    @Context
    private HttpServletResponse response;

    @Inject
    private EZHttpServer server;

    @Inject
    private ProcessManager processManager;

    @Inject
    private EzServerState ezServerState;

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping(){
        return "pong";
    }

    @GET
    @Path("/main")
    @Produces(MediaType.APPLICATION_JSON)
    public WebData getMainData() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        new RulesVersionManager(settingsManager.getEzLoadRepoDir(), mainSettings)
                .initRepoIfNeeded();
        EzProfil ezProfil = null;
        try {
            ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        EZActionManager actionManager = mainSettings.getEzLoad().getEZActionManager(settingsManager);
        ActionWithMsg actionWithMsg = actionManager.refreshAllEZSharesWithMessages();
        actionWithMsg.getErrors().addAll(ezServerState.getDetailedActionErrors());
        return new WebData(new File(SettingsManager.searchConfigFilePath()).getParentFile().getAbsolutePath(),
                            mainSettings,
                            ezProfil,
                            processManager.getLatestProcess(),
                            ezServerState.isProcessRunning(),
                            ezServerState.getEzReports(),
                            actionManager.getIncompleteSharesOrNew(),
                            ezServerState.getFilesNotYetLoaded(),
                            new RulesManager(settingsManager, mainSettings).getAllRules()
                                    .stream()
                                    .map(e -> (RuleDefinitionSummary)e)
                                    .collect(Collectors.toList()),
                            SettingsManager.getVersion(),
                            settingsManager.listAllEzProfiles(),
                            actionWithMsg
                );
    }

    @POST
    @Path("/saveMainSettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MainSettings saveMainSettings(MainSettings mainSettings) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings oldSettings = settingsManager.loadProps();
        if (!oldSettings.getActiveEzProfilName().equals(mainSettings.getActiveEzProfilName())){
            ezServerState.clear();
        }
        settingsManager.saveMainSettingsFile(mainSettings);
        return mainSettings.validate();
    }

    @POST
    @Path("/renameEzProfile")
    @Consumes(MediaType.APPLICATION_JSON)
    public void renameEzProfile(@NotNull @QueryParam("oldProfile") String oldProfileName,
                             @NotNull @QueryParam("newProfile") String newRawProfileName) throws Exception {

        String newProfileName = com.pascal.ezload.service.util.StringUtils.cleanFileName(newRawProfileName);
        if (oldProfileName.equals(newProfileName) || StringUtils.isBlank(newProfileName)) return;
        SettingsManager settingsManager = SettingsManager.getInstance();
        if (StringUtils.isBlank(oldProfileName)){
            settingsManager.newEzProfil(newProfileName);
        }
        else{
            settingsManager.renameEzProfile(oldProfileName, newProfileName);
        }
    }

    @PUT
    @Path("/saveEzProfile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EzProfil saveEzProfile(EzProfil ezProfil) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        String ezProfilName = settingsManager.loadProps().getActiveEzProfilName();
        settingsManager.saveEzProfilFile(ezProfilName, ezProfil);
        return ezProfil.validate();
    }

    @DELETE
    @Path("/deleteEzProfile")
    public void deleteEzProfile(@NotNull @QueryParam("profile") String ezProfilName) throws Exception {
        if (!StringUtils.isBlank(ezProfilName)) {
            SettingsManager settingsManager = SettingsManager.getInstance();
            settingsManager.deleteEzProfil(ezProfilName);
        }
    }


    @POST
    @Path("/gdrive-security-file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void uploadGDriveSecurityFile( @FormDataParam("file")  InputStream file) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        FileUtil.string2file(settingsManager.getGDriveCredsFile(SettingsManager.getActiveEzProfileName(mainSettings)), FileUtil.inputStream2String(file));
    }

    @POST
    @Path("/saveShareValue")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveShareValue(@NotNull @QueryParam("index") int index, EZShare shareValue) throws Exception {
        ezServerState.setEzActionDirty(true);
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        EZActionManager actionManager = mainSettings.getEzLoad().getEZActionManager(settingsManager);
        actionManager.update(index, shareValue);
    }

    @POST
    @Path("/createShareValue")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createShareValue() throws Exception {
        ezServerState.setEzActionDirty(true);
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        EZActionManager actionManager = mainSettings.getEzLoad().getEZActionManager(settingsManager);
        actionManager.newShare();
    }

    @DELETE
    @Path("/deleteShareValue")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteShareValue(@NotNull @QueryParam("index") int index) throws Exception {
        ezServerState.setEzActionDirty(true);
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        EZActionManager actionManager = mainSettings.getEzLoad().getEZActionManager(settingsManager);
        actionManager.deleteShare(index);
    }


    @GET
    @Path("checkAllShares")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess checkAllShares() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        final MainSettings mainSettings = settingsManager.loadProps();
        EZActionManager actionManager = mainSettings.getEzLoad().getEZActionManager(settingsManager);
        return processManager.createNewRunningProcess(settingsManager, mainSettings,
                "Vérification des actions",
                ProcessManager.getLog(mainSettings, "checkShares", ".html"),
                (processLogger) -> {
                    ezServerState.setDetailedActionErros(new LinkedList<>());
                    try (Reporting reporting = processLogger.getReporting().pushSection("Vérification des "+actionManager.getAllEZShares().size()+" actions")) {
                        actionManager.getAllEZShares()
                                .forEach(ezShare -> {
                                    try {
                                        ezServerState.getDetailedActionErrors().addAll(actionManager.computeActionErrors(reporting, ezShare));
                                    } catch (IOException e) {
                                        processLogger.getReporting().error(e);
                                    }
                                });
                    }
                    ActionWithMsg actionWithMsg = actionManager.refreshAllEZSharesWithMessages();
                    if (actionWithMsg.getErrors().size() == 0 && ezServerState.getDetailedActionErrors().size() == 0) {
                        processLogger.getReporting().info("Pas de problème détecté");
                    }
                    else {
                        actionWithMsg.getErrors()
                                .forEach(processLogger.getReporting()::error);
                        ezServerState.getDetailedActionErrors()
                                .forEach(processLogger.getReporting()::error);
                    }
                });
    }


    @PUT
    @Path("/moveConfigDir")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess moveConfigDir(@NotNull @QueryParam("newConfigDir") String newConfigDir) throws Exception {
        SettingsManager oldSettingsManager = SettingsManager.getInstance();
        final MainSettings oldMainSettings = oldSettingsManager.loadProps();
        return processManager.createNewRunningProcess(oldSettingsManager, oldMainSettings,
                "Changement des fichiers de "+SettingsManager.searchConfigFilePath()+" vers "+newConfigDir,
                ProcessManager.getLog(oldMainSettings, "config", "-moveConfigFile.html"),
                (processLogger) -> {
                    if (new File(newConfigDir).exists()) {
                        processLogger.getReporting().error("La destination existe déjà");
                        return;
                    }
                    if (new File(oldSettingsManager.getEzHome()).getAbsolutePath().contains(new File(newConfigDir).getAbsolutePath())) {
                        processLogger.getReporting().error("La destination n'est pas correct");
                        return;
                    }

                    MainSettings newMainSettings = new MainSettings();

                    // Chrome Settings
                    newMainSettings.setChrome(oldMainSettings.getChrome());
                    newMainSettings.getChrome().setUserDataDir(null); // it will be automatically created lazily

                    newMainSettings.setActiveEzProfilName(oldMainSettings.getActiveEzProfilName());

                    newMainSettings.setEzLoad(oldMainSettings.getEzLoad());
                    newMainSettings.getEzLoad().setLogsDir(oldMainSettings.getEzLoad().getLogsDir());
                    newMainSettings.getEzLoad().setCacheDir(oldMainSettings.getEzLoad().getCacheDir());

                    ezServerState.clear();

                    // save the new File
                    String newConfigFilePath = newConfigDir + File.separator + SettingsManager.EZLOAD_CONFIG_YAML;
                    new File(newConfigDir).mkdirs();
                    SettingsManager newSettingsManager = new SettingsManager(newConfigFilePath);
                    newSettingsManager.saveMainSettingsFile(newMainSettings);

                    // reload it, this will initialize the properties that are not correctly set
                    newMainSettings = newSettingsManager.loadProps();

                    // reload the originals settings, because the mainSettings object was dirty
                    MainSettings oldMainSettingsReloaded = oldSettingsManager.loadProps();

                    try {
                        FileUtil.copyFile(oldSettingsManager.getDashboardFile(), newSettingsManager.getDashboardFile());
                    }
                    catch (Exception e){
                        processLogger.getReporting().info("Erreur lors de la copie du fichier dashboard "+e.getMessage());
                    }

                    try {
                        new RulesVersionManager(newSettingsManager.getEzLoadRepoDir(), newMainSettings).initRepoIfNeeded(); // create the git repo
                        // then copy the rules in case there are some custom rules
                        FileUtil.copyDir(oldSettingsManager.getRulesDir(), newSettingsManager.getRulesDir());
                    }
                    catch (Exception e){
                        processLogger.getReporting().error("Erreur lors de la copie des règles "+e.getMessage());
                    }

                    try {
                        FileUtil.copyFile(oldSettingsManager.getShareDataFile(), newSettingsManager.getShareDataFile());
                    }
                    catch (Exception e){
                        processLogger.getReporting().error("Erreur lors de la copie du fichiers de description des actions "+e.getMessage());
                    }

                    oldSettingsManager.listAllEzProfiles()
                            .forEach(profile -> {
                                try {
                                    EzProfil profil = oldSettingsManager.readEzProfilFile(profile);
                                    newSettingsManager.saveEzProfilFile(profile, profil);

                                    FileUtil.copyDir(oldSettingsManager.getEzProfileDirectory(profile), newSettingsManager.getEzProfileDirectory(profile));
                                } catch (Exception e) {
                                    processLogger.getReporting().error("Erreur lors de la copie du profile: "+profile);
                                }
                            });

                    newSettingsManager.moveDone();

                    // clean old config dir

                    // clean old config dir
                    try(Stream<java.nio.file.Path> stream = Files.walk(Paths.get(oldSettingsManager.getEzHome()), 1)) {
                        stream
                                .forEach(f -> {
                                    if (!f.toFile().getAbsolutePath().equals(oldSettingsManager.getConfigFile()) && !f.toFile().getAbsolutePath().equals(oldSettingsManager.getEzHome())){
                                        try {
                                            if (f.toFile().isDirectory()){
                                                FileUtil.rmdir(f.toFile());
                                            }
                                            else {
                                                f.toFile().delete();
                                            }
                                        } catch (IOException e) {
                                        }
                                    }
                                });
                    }
                });
    }

    @GET
    @Path("/searchAccounts")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess searchAccounts(@NotNull @QueryParam("courtier") EnumEZBroker courtier,
                                    @NotNull @QueryParam("chromeVersion") String chromeVersion) throws Exception {
        if (courtier != EnumEZBroker.BourseDirect) {
            throw new IllegalArgumentException("Cette operation n'est pas encore développé pour le courtier: "+courtier.getEzPortfolioName());
        }
        else {
            SettingsManager settingsManager = SettingsManager.getInstance();
            MainSettings mainSettings = settingsManager.loadProps();
            EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
            return processManager.createNewRunningProcess(settingsManager, mainSettings,
                    "Recherche de Nouveaux Comptes "+courtier.getEzPortfolioName(),
                    ProcessManager.getLog(mainSettings, courtier.getDirName(), "-searchAccount.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
                    List<BourseDirectEZAccountDeclaration> accountsExtracted =
                            new BourseDirectSearchAccounts(settingsManager, mainSettings, ezProfil, reporting).extract(chromeVersion);
                    // copy the accounts into the main settings

                    List<BourseDirectEZAccountDeclaration> accountsDefined = ezProfil.getBourseDirect().getAccounts();
                    reporting.info(accountsDefined.size()+" Compte(s) trouvé(s)");
                    accountsExtracted.stream()
                            .filter(acc ->
                                    accountsDefined
                                            .stream()
                                            .noneMatch(accDefined -> accDefined.getNumber().equals(acc.getNumber())))
                            .forEach(newAccount -> {
                                        reporting.info("Ajout du compte: "+newAccount.getNumber()+" de "+newAccount.getName());
                                        ezProfil.getBourseDirect().getAccounts().add(newAccount);
                                    });
                    settingsManager.saveEzProfilFile(mainSettings.getActiveEzProfilName(), ezProfil);
                }
            );
        }
    }

    @POST
    @Path("/exit")
    public void exit() {
        processManager.kill();
        server.stop();
    }


    @GET
    @Path("/viewProcess")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public void viewLogProcess() throws IOException {
        try(Writer writer = response.getWriter()) {
            processManager.viewLogProcess(writer);
        }
    }

    @POST
    @Path("/checkUpdate")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess checkUpdate() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        return processManager.createNewRunningProcess(settingsManager, mainSettings,
                "Recherche de Mise à jour",
                ProcessManager.getLog(mainSettings, "update", "-check.html"),
                (processLogger) -> {
                    RulesVersionManager rulesVersionManager = new RulesVersionManager(settingsManager.getEzLoadRepoDir(), mainSettings);
                    rulesVersionManager.synchSharedRulesFolder(processLogger.getReporting());
                });
    }

}
