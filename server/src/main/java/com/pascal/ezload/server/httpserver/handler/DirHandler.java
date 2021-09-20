package com.pascal.ezload.server.httpserver.handler;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.pascal.ezload.service.config.SettingsManager;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/browse")
public class DirHandler {

    public static final String REPORT_FILE_PREFIX =  "bientotRentier-report-";
    public static final String REPORT_FILE_SUFFIX =  ".html";

    @Context
    private HttpServletResponse context;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/file/{file}")
    public String view(@NotNull @PathParam("file") String file) throws IOException {
        String contentType = file.toLowerCase(Locale.ROOT).endsWith("pdf") ? "application/pdf" : "text/html";
        OutputStream os = context.getOutputStream();
        String dir = ""; // TO DO
        IOUtils.copy(new FileInputStream(dir+file), os);
        os.close();
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dir/{currentDir}")
    public List<String> list(@NotNull @PathParam("currentDir") String currentDir) throws Exception {
            String dir = SettingsManager.getInstance().loadProps().getEZLoad().getLogsDir();
            File listFiles[] = new File(dir + currentDir).listFiles();
            List<File> allFiles =  listFiles == null ? new ArrayList<>() : Arrays.asList(listFiles);
            List<File> dirs = allFiles.stream()
                    .filter(File::isDirectory)
                    .filter(f -> !f.getName().startsWith("."))
                    .filter(dirFilter())
                    .sorted()
                    .collect(Collectors.toList());
            List<File> files = allFiles.stream()
                    .filter(File::isFile)
                    .filter(fileFilter())
                    .sorted(Comparator.comparing(File::getName).reversed())
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            String back = currentDir.equals("") ? null : // home
                    new File(dir+currentDir).getParent()
                            .substring(dir.length())
                            .replace('\\', '/'); // for windows only
            if ("".equals(back)) back = "/"; // root
            data.put("fileTarget", null);
            data.put("back", back);
            data.put("currentDir", currentDir);
            data.put("context", context);
            data.put("dir", dir);
            data.put("files", files);
            data.put("dirs", dirs);
//                dirTemplateMustache.execute(os, data);
      //      os.close();
            return null;
    }

    public static Predicate<File> fileFilter(){
        return file -> file.getName().startsWith(REPORT_FILE_PREFIX) && file.getName().endsWith(REPORT_FILE_SUFFIX);
    }

    public static Predicate<File> dirFilter(){
        return file -> false; // there is no subdir for log
    }

}
