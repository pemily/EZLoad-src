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
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("explorer")
public class ExplorerHandler {

    @GET
    @Path("/file")
    public Response SourceFile(@NotNull @QueryParam("source") String sourceFile) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        String ezProfilName = SettingsManager.getActiveEzProfileName(mainSettings);
        File file = new File(settingsManager.getDownloadDir(ezProfilName, EnumEZBroker.BourseDirect)+File.separator+sourceFile);
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
        StreamingOutput output = out -> {
            int length;
            byte[] buffer = new byte[1024];
            while((length = input.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.flush();
            input.close();
        };
        Response.ResponseBuilder responseBuilder = Response.ok(output);
        String[] fileWithExtension = StringUtils.divide(sourceFile, '.');
        if (fileWithExtension.length == 2) responseBuilder.type("application/"+fileWithExtension[1]);
        else responseBuilder.type("application/stream");
        responseBuilder.header("Content-Disposition", "filename=\"" + sourceFile + "\"");
        return responseBuilder.build();
    }

    @GET
    @Path("/dir")
    @Produces("application/json")
    public List<Item> list(@Nullable @QueryParam("dirpath") String dir) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        String ezProfilName = SettingsManager.getActiveEzProfileName(mainSettings);
        String subDir = StringUtils.isBlank(dir) ? "" : (dir.startsWith(".") || dir.startsWith("..") ? "" : File.separator+dir);
        File file = new File(settingsManager.getDownloadDir(ezProfilName, EnumEZBroker.BourseDirect)+subDir);
        if (file.isDirectory()){
            return Arrays.stream(file.listFiles())
                    .map(f -> f.isDirectory() ? new Item(f.getName(), true) : new Item(f.getName(), false))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static class Item {
        private String name;
        private boolean isDir;

        public Item(){
        }

        public Item(String name, boolean isDir){
            this.name = name;
            this.isDir = isDir;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isDir() {
            return isDir;
        }

        public void setDir(boolean dir) {
            isDir = dir;
        }
    }

}