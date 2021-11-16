package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("explorer")
public class ExplorerHandler {

    @GET
    @Path("/file")
    @Produces("application/pdf")
    public Response SourceFile(@NotNull @QueryParam("source") String sourceFile) throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        File file = new File(mainSettings.getEzLoad().getDownloadDir()+File.separator+sourceFile);
        FileInputStream fileInputStream = new FileInputStream(file);
        Response.ResponseBuilder responseBuilder = Response.ok(fileInputStream);
        responseBuilder.type("application/pdf");
        responseBuilder.header("Content-Disposition","filename=\"" + sourceFile + "\"");
        return responseBuilder.build();
    }

    @GET
    @Path("/dir")
    @Produces("application/json")
    public List<Item> list(@Nullable @QueryParam("dirpath") String dir) throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        String subDir = StringUtils.isBlank(dir) ? "" : (dir.startsWith(".") || dir.startsWith("..") ? "" : File.separator+dir);
        File file = new File(mainSettings.getEzLoad().getDownloadDir()+subDir);
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