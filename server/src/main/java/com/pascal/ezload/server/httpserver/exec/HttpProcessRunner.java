package com.pascal.ezload.server.httpserver.exec;

import com.pascal.ezload.service.util.FileLinkCreator;
import com.pascal.ezload.service.util.HtmlReporting;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HttpProcessRunner implements Closeable {

    public static final String FILE_HEADER = "<!-- END OF FILE HEADER -->";
    public static final String FILE_FOOTER = "<!-- START OF FILE FOOTER -->";

    private final HtmlReporting reporting;
    private final Writer logFileWriter;

    public HttpProcessRunner(Writer logFileWriter, FileLinkCreator linkCreator){
        this.reporting = new HtmlReporting(linkCreator, logFileWriter);
        this.logFileWriter = logFileWriter;
    }

    public void header(String escapedTitle) throws IOException {
        logFileWriter.write("<html><head><meta charset='UTF-8'>\n");
        reporting.writeHeader(escapedTitle+"<BR>"+reporting.escape(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())));
        logFileWriter.write("</head><body>\n"+FILE_HEADER+"\n");
    }

    public HtmlReporting getReporting(){
        return reporting;
    }

    @Override
    public void close() throws IOException {
        reporting.close();
        logFileWriter.write("\n"+FILE_FOOTER+"\n</body></html>\n");
        logFileWriter.flush();
        logFileWriter.close();
    }
}
