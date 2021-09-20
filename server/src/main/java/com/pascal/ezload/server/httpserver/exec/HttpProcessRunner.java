package com.pascal.ezload.server.httpserver.exec;

import com.pascal.ezload.service.util.FileLinkCreator;
import com.pascal.ezload.service.util.HtmlReporting;
import com.pascal.ezload.service.util.MultiWriter;

import java.io.*;

public class HttpProcessRunner implements Closeable {

    private final EzProcess process;
    private final HtmlReporting reporting;
    private final Writer fileWriter;


    public HttpProcessRunner(EzProcess process, Writer htmlPageWriter, FileLinkCreator linkCreator) throws IOException {
        this.process = process;
        this.fileWriter = new BufferedWriter(new FileWriter(process.getLogFile()));
        this.reporting = new HtmlReporting(linkCreator, new MultiWriter(fileWriter, htmlPageWriter));  // will write into the report & html Page
    }

    public void header(String escapedTitle) throws IOException {
        fileWriter.write("<html><head><meta charset='UTF-8'>\n");
        reporting.writeHeader(escapedTitle);
        fileWriter.write("</head><body>\n");
    }

    public HtmlReporting getReporting(){
        return reporting;
    }

    @Override
    public void close() throws IOException {
        reporting.close();
        fileWriter.write("</body></html>\n");
        fileWriter.flush();
        fileWriter.close();
        process.setRunning(false);
    }
}
