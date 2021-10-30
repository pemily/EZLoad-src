package com.pascal.ezload.service.util;

import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;

public class HtmlReporting implements Reporting {

    private static final String EZ_LOAD_HEADER_REPORTING_JS = "ezLoadHeaderReporting.js";
    private static final String EZ_LOAD_HEADER_REPORTING_CSS = "ezLoadHeaderReporting.css";

    private final Writer writer;
    private final FileLinkCreator fileLinkCreator;

    public HtmlReporting(FileLinkCreator fileLinkCreator, Writer writer){
        this.writer = writer;
        this.fileLinkCreator = fileLinkCreator;
    }

    public void writeHeader(String escapedTitle){
        try {
            writer.write("<script src='https://code.jquery.com/jquery-3.4.1.min.js'></script>");
            writer.write("<script>");
            try(Reader r = new InputStreamReader(asStream(EZ_LOAD_HEADER_REPORTING_JS))) {
                IOUtils.copy(r, writer);
            }
            writer.write("</script>");
            writer.write("<style type='text/css'>");
            try(Reader r = new InputStreamReader(asStream(EZ_LOAD_HEADER_REPORTING_CSS))) {
                IOUtils.copy(r, writer);
            }
            writer.write("</style>");
            writer.write("<h1><center>"+ escapedTitle+"</center></h1>\n");
            writer.write("<ul id='1' class='br-tree'></ul>\n");
            writer.flush();
        } catch (IOException e) {
            // Ignore exception if the log file cannot be written (html page is perhaps lost)
        }
    }

    public InputStream asStream(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    @Override
    public void error(Throwable error) {
        StringWriter sw = new StringWriter();
        error.printStackTrace(new PrintWriter(sw));
        write(sw.toString(), true);
    }

    @Override
    public void error(String error) {
        write(error, true);
    }

    @Override
    public void info(String info) {
        write(info, false);
    }

    @Override
    public HtmlReporting pushSection(String sectionTitle) {
        try {
            writer.write("<script>pushSection(\""+ escape(sectionTitle)+"\")</script>\n");
            writer.flush();
        } catch (IOException e) {
            // Ignore exception if the log file cannot be writer (html page is perhaps lost)
        }
        return this;
    }

    @Override
    public HtmlReporting pushSection(TitleWithFileRef sectionTitle) {
        try {
            writer.write("<script>pushSection(\""+ sectionTitle.format(this, fileLinkCreator)+"\")</script>\n");
            writer.flush();
        } catch (IOException e) {
            // Ignore exception if the log file cannot be writer (html page is perhaps lost)
        }
        return this;
    }

    @Override
    public void popSection() {
        try {
            writer.write("<script>popSection()</script>\n");
            writer.flush();
        } catch (IOException e) {
            // Ignore exception if the log file cannot be writer (html page is perhaps lost)
        }
    }


    private void write(String text, boolean isError){
        try {
            writer.write( "<script>add(\""+ escape(text)+"\", "+isError+")</script>\n");
            writer.flush();
        }
        catch(IOException e){
            // Ignore exception if the log file cannot be writer (html page is perhaps lost)
        }
    }

    public String escape(String text){
        return StringEscapeUtils
                .escapeHtml4(text)
                .replace("\t", "&nbsp;&nbsp;")
                .replace("\r", "")
                .replace("\n", "<br>")
                .replace("\"", "&quot;")
                .replace("\\", "&#92;")
                ;
    }

}
