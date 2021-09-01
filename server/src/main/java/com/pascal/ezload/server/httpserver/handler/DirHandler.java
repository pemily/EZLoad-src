package com.pascal.ezload.server.httpserver.handler;


import com.pascal.ezload.service.config.MainSettings;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path("/browse")
public class DirHandler {

    public static final String REPORT_FILE_PREFIX =  "bientotRentier-report-";
    public static final String REPORT_FILE_SUFFIX =  ".html";

    @Context
    private HttpServletResponse context;

    @Inject
    private MainSettings mainSettings;

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
    public List<String> list(@NotNull @PathParam("currentDir") String currentDir) throws IOException {
            String dir = mainSettings.getEZLoad().getLogsDir();
            List<File> dirs = Arrays.asList(new File(dir + currentDir).listFiles()).stream()
                    .filter(File::isDirectory)
                    .filter(f -> !f.getName().startsWith("."))
                    .filter(dirFilter())
                    .sorted()
                    .collect(Collectors.toList());
            List<File> files = Arrays.asList(new File(dir + currentDir).listFiles()).stream()
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
