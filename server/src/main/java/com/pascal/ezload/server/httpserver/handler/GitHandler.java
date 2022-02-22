package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.rules.update.FileStatus;
import com.pascal.ezload.service.rules.update.RulesVersionManager;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.io.File;
import java.util.List;

@Path("git")
public class GitHandler {

    @GET
    @Path("/changes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FileStatus> getChanges() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        return new RulesVersionManager(settingsManager.getEzLoadRepoDir(), mainSettings)
                .getAllChanges(mainSettings.getEzLoad().getRulesDir());

    }

    @GET
    @Path("/change")
    @Produces(MediaType.TEXT_PLAIN)
    public String getChange(@NotNull @QueryParam("file") String gitFilePath) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        return new RulesVersionManager(settingsManager.getEzLoadRepoDir(), mainSettings)
                .getChange(settingsManager.getEzLoadRepoDir()+"/"+gitFilePath);

    }


    @DELETE
    @Path("/revert")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void revert(@NotNull @QueryParam("file") String gitFile) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        new RulesVersionManager(settingsManager.getEzLoadRepoDir(), mainSettings)
                .revert(settingsManager.getEzLoadRepoDir() + File.separator + gitFile);
    }



    @POST
    @Path("/push")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void push(@NotNull @QueryParam("message")  String message) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        new RulesVersionManager(settingsManager.getEzLoadRepoDir(), mainSettings)
                .commitAndPush(mainSettings.getEzLoad().getAdmin().getEmail(), message);
    }
}
