package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.sources.Reporting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;

public class HtmlReporting implements Reporting {

    private final Writer writer;
    private final String title;

    public HtmlReporting(String title, Writer writer){
        this.title = title;
        this.writer = writer;
        try {
            IOUtils.copy(new InputStreamReader( getClass().getClassLoader().getResourceAsStream("bientotRentier.html")), writer);
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
            writer.write("<script>pushSection(\""+esc(sectionTitle)+"\")</script>\n");
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
            writer.write( "<script>add(\""+esc(text)+"\","+isError+")</script>\n");
            writer.flush();
        }
        catch(IOException e){
            throw new BRException(e);
        }
    }

    private String esc(String text){
        return StringEscapeUtils
                .escapeHtml3(text)
                .replace("\n", "<br>")
                .replace("\"", "&quot;")
                ;
    }

}
