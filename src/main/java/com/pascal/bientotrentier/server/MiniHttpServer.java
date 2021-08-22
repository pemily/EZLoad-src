package com.pascal.bientotrentier.server;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.util.BRException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class MiniHttpServer implements Closeable {
    private HttpServer server;
    private MustacheFactory mustacheFactory; // https://www.baeldung.com/mustache
    private Mustache homeTemplateMustache;
    private Mustache dirTemplateMustache;

    public int start(MainSettings mainSettings) throws Exception {
        mustacheFactory = new DefaultMustacheFactory();
        homeTemplateMustache = mustacheFactory.compile("homeTemplate.mustache");
        dirTemplateMustache = mustacheFactory.compile("dirTemplate.mustache");

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/home", new HomeHandler(mainSettings));
        server.createContext("/logs", new DirHandler("logs", mainSettings.getBientotRentier().getLogsDir()));
        server.createContext("/bourseDirectDir", new DirHandler("bourseDirectDir", mainSettings.getBourseDirect().getPdfOutputDir()));
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
            header.add("Connection", "Keep-Alive");
            header.add("Keep-Alive", "timeout=30 max=100");
            header.add("Content-Type", "text/html");
            exchange.sendResponseHeaders( 200, 0 );
            Writer os = new OutputStreamWriter(exchange.getResponseBody());
            new StartAction(mainSettings).start(os);
            os.close();
        }
    }

    private class ExitActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers header = exchange.getResponseHeaders();
            header.add("Content-Type", "text/html");
            exchange.sendResponseHeaders( 200, 0 );
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
            exchange.sendResponseHeaders( 200, 0 );
            Writer os = new OutputStreamWriter(exchange.getResponseBody());
            homeTemplateMustache.execute(os, mainSettings);
            os.close();
        }
    }


    private class DirHandler implements HttpHandler {
        private String dir;
        private String context;

        public DirHandler(String context, String dir) {
            this.dir = dir;
            this.context = context;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers header = exchange.getResponseHeaders();

            String currentDir = getValue(exchange, "dir");
            if (currentDir.equals("/")) currentDir = "";
            String file = getValue(exchange, "file");

            if (StringUtils.isBlank(file)) {
                header.add("Content-Type", "text/html");
                exchange.sendResponseHeaders( 200, 0 );
                Writer os = new OutputStreamWriter(exchange.getResponseBody());

                List<File> dirs = Arrays.asList(new File(dir + currentDir).listFiles()).stream()
                        .filter(File::isDirectory)
                        .filter(f -> !f.getName().startsWith("."))
                        .collect(Collectors.toList());
                List<File> files = Arrays.asList(new File(dir + currentDir).listFiles()).stream()
                        .filter(File::isFile)
                        .filter(f -> !f.getName().startsWith("."))
                        .collect(Collectors.toList());

                Map<String, Object> data = new HashMap<>();
                String back = currentDir.equals("") ? null : // home
                                    new File(dir+currentDir).getParent()
                                        .substring(dir.length())
                                            .replace('\\', '/'); // for windows only
                if ("".equals(back)) back = "/"; // root
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
                exchange.sendResponseHeaders( 200, 0 );
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
}
