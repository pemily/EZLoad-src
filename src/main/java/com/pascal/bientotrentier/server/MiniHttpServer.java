package com.pascal.bientotrentier.server;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.SettingsManager;
import com.pascal.bientotrentier.model.EnumBRCourtier;
import com.pascal.bientotrentier.security.AuthManager;
import com.pascal.bientotrentier.sources.bourseDirect.download.BourseDirectDownloader;
import com.pascal.bientotrentier.util.BRException;
import com.pascal.bientotrentier.util.FileLinkCreator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MiniHttpServer implements Closeable {
    private static final String PDF_BOURSE_DIRECT_CONTEXT = "/bourseDirectDir";
    private static final String PDF_BOURSE_DIRECT_TARGET = "bourseDirectPdf";
    private static final String LOGS_CONTEXT = "/logs";
    private static final String LOGS_TARGET = "log";

    private HttpServer server;
    private Mustache homeTemplateMustache;
    private Mustache dirTemplateMustache;

    public int start(MainSettings mainSettings) throws Exception {
        // https://www.baeldung.com/mustache
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        homeTemplateMustache = mustacheFactory.compile("homeTemplate.mustache");
        dirTemplateMustache = mustacheFactory.compile("dirTemplate.mustache");

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/home", new HomeHandler(mainSettings));
        server.createContext("/createCreds", new CreateCredentialsHandler(mainSettings));
        server.createContext(LOGS_CONTEXT, new DirHandler(LOGS_CONTEXT, mainSettings.getBientotRentier().getLogsDir(),
                StartAction.dirFilter(),
                StartAction.fileFilter(),
                LOGS_TARGET));
        server.createContext(PDF_BOURSE_DIRECT_CONTEXT, new DirHandler(PDF_BOURSE_DIRECT_CONTEXT, mainSettings.getBourseDirect().getPdfOutputDir(),
                BourseDirectDownloader.dirFilter(mainSettings),
                BourseDirectDownloader.fileFilter(),
                PDF_BOURSE_DIRECT_TARGET));
        server.createContext("/action/start", new StartActionHandler(mainSettings));
        server.createContext("/action/exit", new ExitActionHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        return server.getAddress().getPort();
    }


    public void stop(){
        server.stop(5000);
    }

    @Override
    public void close() {
        stop();
    }


    private class StartActionHandler implements HttpHandler {
        private final MainSettings mainSettings;

        protected StartActionHandler(MainSettings mainSettings) {
            this.mainSettings = mainSettings;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers header = exchange.getResponseHeaders();
            String simulation = getValue(exchange, "simulation");
            header.add("Connection", "Keep-Alive");
            header.add("Keep-Alive", "timeout=30 max=100");
            header.add("Content-Type", "text/html");
            exchange.sendResponseHeaders( HttpStatus.SC_OK, 0 );
            Writer os = new OutputStreamWriter(exchange.getResponseBody());
            new StartAction(mainSettings).start(os, fileLinkCreator(mainSettings), Boolean.parseBoolean(simulation));
            os.close();
        }
    }

    private class ExitActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers header = exchange.getResponseHeaders();
            header.add("Content-Type", "text/html");
            exchange.sendResponseHeaders( HttpStatus.SC_OK, 0 );
            Writer os = new OutputStreamWriter(exchange.getResponseBody());
            os.write("<h1>Bientot Rentier</h1>");
            os.write("à Bientôt");
            os.close();
            server.stop(5);
        }
    }

    private class HomeHandler implements HttpHandler {
        private final MainSettings mainSettings;

        protected HomeHandler(MainSettings mainSettings) {
            this.mainSettings = mainSettings;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers header = exchange.getResponseHeaders();
            header.add("Content-Type", "text/html");
            exchange.sendResponseHeaders( HttpStatus.SC_OK, 0 );
            Writer os = new OutputStreamWriter(exchange.getResponseBody());
            homeTemplateMustache.execute(os, mainSettings);
            os.close();
        }
    }


    private class DirHandler implements HttpHandler {
        private final String dir;
        private final String context;
        private final Predicate<File> dirFilter;
        private final Predicate<File> fileFilter;
        private final String fileTarget;

        public DirHandler(String context, String dir, Predicate<File> dirFilter, Predicate<File> fileFilter, String fileTarget) {
            this.dir = dir;
            this.context = context;
            this.dirFilter = dirFilter;
            this.fileFilter = fileFilter;
            this.fileTarget = fileTarget;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers header = exchange.getResponseHeaders();

            String currentDir = getValue(exchange, "dir");
            if (currentDir.equals("/")) currentDir = "";
            String file = getValue(exchange, "file");

            if (StringUtils.isBlank(file)) {
                header.add("Content-Type", "text/html");
                exchange.sendResponseHeaders( HttpStatus.SC_OK, 0 );
                Writer os = new OutputStreamWriter(exchange.getResponseBody());

                List<File> dirs = Arrays.asList(new File(dir + currentDir).listFiles()).stream()
                        .filter(File::isDirectory)
                        .filter(f -> !f.getName().startsWith("."))
                        .filter(dirFilter)
                        .sorted()
                        .collect(Collectors.toList());
                List<File> files = Arrays.asList(new File(dir + currentDir).listFiles()).stream()
                        .filter(File::isFile)
                        .filter(fileFilter)
                        .sorted(Comparator.comparing(File::getName).reversed())
                        .collect(Collectors.toList());

                Map<String, Object> data = new HashMap<>();
                String back = currentDir.equals("") ? null : // home
                                    new File(dir+currentDir).getParent()
                                        .substring(dir.length())
                                            .replace('\\', '/'); // for windows only
                if ("".equals(back)) back = "/"; // root
                data.put("fileTarget", fileTarget);
                data.put("back", back);
                data.put("currentDir", currentDir);
                data.put("context", context);
                data.put("dir", dir);
                data.put("files", files);
                data.put("dirs", dirs);
                dirTemplateMustache.execute(os, data);
                os.close();
            }
            else{
                String contentType = file.toLowerCase(Locale.ROOT).endsWith("pdf") ? "application/pdf" : "text/html";
                header.add("Content-Type", contentType);
                exchange.sendResponseHeaders( HttpStatus.SC_OK, 0 );
                OutputStream os = exchange.getResponseBody();

                IOUtils.copy(new FileInputStream(dir+file), os);
                os.close();
            }
        }
    }

    public String getValue(HttpExchange exchange, String param){
        try {
            Optional<String> currentDirOpt = new URIBuilder(exchange.getRequestURI().toString())
                    .getQueryParams()
                    .stream()
                    .filter(p -> p.getName().equals(param))
                    .map(NameValuePair::getValue)
                    .findFirst();
            return currentDirOpt.orElse("");
        } catch (URISyntaxException e) {
            throw new BRException(e);
        }
    }


    public FileLinkCreator fileLinkCreator(MainSettings mainSettings){
        return (reporting, sourceFile) -> {
            if (sourceFile.startsWith(mainSettings.getBourseDirect().getPdfOutputDir())){
                String file = sourceFile.substring(mainSettings.getBourseDirect().getPdfOutputDir().length());
                file = file.replace('\\', '/'); // pour windows
                return "<a target='"+PDF_BOURSE_DIRECT_TARGET+"' href='"+PDF_BOURSE_DIRECT_CONTEXT+"?file="+file+"'>"+ reporting.escape(file)+"</a>";
            }
            else if (sourceFile.startsWith(mainSettings.getBientotRentier().getLogsDir())){
                String file = sourceFile.substring(mainSettings.getBientotRentier().getLogsDir().length());
                file = file.replace('\\', '/'); // pour windows
                return "<a target='"+LOGS_TARGET+"' href='"+LOGS_CONTEXT+"?file="+file+"'>"+ reporting.escape(file)+"</a>";
            }
            else{
                return reporting.escape(sourceFile);
            }
        };
    }

    private class CreateCredentialsHandler implements HttpHandler {
        private MainSettings mainSettings;

        public CreateCredentialsHandler(MainSettings mainSettings) {
            this.mainSettings = mainSettings;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = getPostParams(exchange);
            String user = params.get("username");
            String password = params.get("password");
            String courtier = params.get("courtier");
            if (!StringUtils.isBlank(user) && !StringUtils.isBlank(password) && !StringUtils.isBlank(courtier)){
                MainSettings.AuthInfo authInfo = new MainSettings.AuthInfo();
                authInfo.setPassword(password);
                authInfo.setUsername(user);
                try {
                    SettingsManager.getAuthManager(mainSettings)
                            .addAuthInfo(EnumBRCourtier.valueOf(courtier), authInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            exchange.getResponseHeaders().set("Location", "/home");
            exchange.sendResponseHeaders(HttpStatus.SC_SEE_OTHER, 0 );
            exchange.getResponseBody().close();
        }
    }

    private Map<String, String> getPostParams(HttpExchange exchange) throws IOException {
        InputStreamReader isr =  new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String params = br.readLine();
        String[] allParams = params.split("&");
        Map<String, String> result = new HashMap<>();
        for (String allParam : allParams) {
            String[] nameValue = allParam.split("=");
            result.put(URLDecoder.decode(nameValue[0], StandardCharsets.UTF_8.toString()), URLDecoder.decode(nameValue[1], StandardCharsets.UTF_8.toString()));
        }
        return result;
    }
}
