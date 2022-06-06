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
                .commitAndPush(mainSettings.getEzLoad().getAdmin().getEmail(), mainSettings.getEzLoad().getAdmin().getBranchName(), message);
    }
}
