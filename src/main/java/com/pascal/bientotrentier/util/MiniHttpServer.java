package com.pascal.bientotrentier.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class MiniHttpServer implements Closeable {
    private HttpServer server;

    public void start(File logFile, String stopSentence) throws Exception {
        server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler(logFile, stopSentence));
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

    static class MyHandler implements HttpHandler {
        private final File logFile;
        private final String stopSentence;

        MyHandler(File logFile, String stopSentence){
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
}
