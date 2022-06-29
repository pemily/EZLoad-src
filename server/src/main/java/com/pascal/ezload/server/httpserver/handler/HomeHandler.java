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
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.rules.update.RulesVersionManager;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectSearchAccounts;
import com.pascal.ezload.service.util.FileUtil;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

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
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        return new WebData(SettingsManager.searchConfigFilePath(),
                            mainSettings,
                            ezProfil,
                            processManager.getLatestProcess(),
                            ezServerState.isProcessRunning(),
                            ezServerState.getEzReports(),
                            ezServerState.getNewShares(),
                            ezServerState.getFilesNotYetLoaded(),
                            new RulesManager(settingsManager.getEzLoadRepoDir(), mainSettings).getAllRules()
                                    .stream()
                                    .map(e -> (RuleDefinitionSummary)e)
                                    .collect(Collectors.toList()),
                            SettingsManager.getVersion(),
                            settingsManager.listAllEzProfiles()
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
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        FileUtil.string2file(ezProfil.getEzPortfolio().getGdriveCredsFile(), FileUtil.inputStream2String(file));
    }

    @POST
    @Path("/saveNewShareValue")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveNewShareValue(ShareValue shareValue) {
        ezServerState.getNewShares().stream()
            .filter(s -> s.getTickerCode().equals(shareValue.getTickerCode())
                    && s.getBroker().equals(shareValue.getBroker())
                    && s.getEzAccountType().equals(shareValue.getEzAccountType())
            )
            .forEach(s -> {
                s.setUserShareName(shareValue.getUserShareName());
                s.setDirty(true);
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
            return processManager.createNewRunningProcess(mainSettings, ezProfil,
                    "Recherche de Nouveaux Comptes "+courtier.getEzPortfolioName(),
                    ProcessManager.getLog(mainSettings, courtier.getDirName(), "-searchAccount.html"),
                (processLogger) -> {
                    Reporting reporting = processLogger.getReporting();
                    List<BourseDirectEZAccountDeclaration> accountsExtracted =
                            new BourseDirectSearchAccounts(mainSettings, ezProfil, reporting).extract(chromeVersion, settingsManager.saveNewChromeDriver());
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
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        return processManager.createNewRunningProcess(mainSettings, ezProfil,
                "Recherche de Mise à jour",
                ProcessManager.getLog(mainSettings, "update", "-check.html"),
                (processLogger) -> {
                    RulesVersionManager rulesVersionManager = new RulesVersionManager(settingsManager.getEzLoadRepoDir(), mainSettings);
                    rulesVersionManager.synchSharedRulesFolder(processLogger.getReporting());
                });
    }

}
