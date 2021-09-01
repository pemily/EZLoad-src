package com.pascal.ezload.service.util;

import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;

public class HtmlReporting implements Reporting {

    private final Writer writer;
    private final FileLinkCreator fileLinkCreator;

    public HtmlReporting(FileLinkCreator fileLinkCreator, Writer writer){
        this.writer = writer;
        this.fileLinkCreator = fileLinkCreator;
    }

    public void writeHeader(String escapedTitle){
        try {
            IOUtils.copy(new InputStreamReader( getClass().getClassLoader().getResourceAsStream("bientotRentier.html")), writer);
            writer.write("<h1><center>"+ escapedTitle+"</center></h1>\n");
            writer.write("<ul id='1' class='br-tree'></ul>\n");
            writer.flush();
        } catch (IOException e) {
            throw new BRException(e);
        }
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
            throw new BRException(e);
        }
        return this;
    }

    @Override
    public HtmlReporting pushSection(TitleWithFileRef sectionTitle) {
        try {
            writer.write("<script>pushSection(\""+ sectionTitle.format(this, fileLinkCreator)+"\")</script>\n");
            writer.flush();
        } catch (IOException e) {
            throw new BRException(e);
        }
        return this;
    }

    @Override
    public void popSection() {
        try {
            writer.write("<script>popSection()</script>\n");
            writer.flush();
        } catch (IOException e) {
            throw new BRException(e);
        }
    }


    private void write(String text, boolean isError){
        try {
            writer.write( "<script>add(\""+ escape(text)+"\","+isError+")</script>\n");
            writer.flush();
        }
        catch(IOException e){
            throw new BRException(e);
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
