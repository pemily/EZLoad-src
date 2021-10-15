package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.FileInputStream;

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


}