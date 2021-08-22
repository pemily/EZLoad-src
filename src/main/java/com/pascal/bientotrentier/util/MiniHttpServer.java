package com.pascal.bientotrentier.util;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class MiniHttpServer implements Closeable {
    private HttpServer server;

    public void start(File logFile, String stopSentence) throws Exception {
        server = HttpServer.create(new InetSocketAddress(8000), 4);
        server.createContext("/bientotRentier/report", new LogHandler(logFile, stopSentence));
        server.createContext("/file", new FileHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }


    public void stop(){
        server.stop(5000);
    }

    @Override
    public void close() {
        stop();
    }

    static class LogHandler implements HttpHandler {
        private final File logFile;
        private final String stopSentence;

        LogHandler(File logFile, String stopSentence){
            this.logFile = logFile;
            this.stopSentence = stopSentence;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers header = t.getResponseHeaders();
            header.add("Connection", "Keep-Alive");
            header.add("Keep-Alive", "timeout=30 max=100");
            header.add("Content-Type", "text/html");
            t.sendResponseHeaders( 200, 0 );
            Writer os = new OutputStreamWriter(t.getResponseBody());
            Tail.tail(logFile, os, true, stopSentence);
        }
    }


    static class FileHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println(t.getRequestURI().toURL());
            System.out.println(t.getRequestURI().toURL().getFile());
            t.sendResponseHeaders( 200, 0 );
            OutputStream os = t.getResponseBody();
            InputStream in = System.class.getResourceAsStream("miniHttp/"+t.getRequestURI().toURL().getFile());
            ByteStreams.copy(in, os);
        }
    }
}
