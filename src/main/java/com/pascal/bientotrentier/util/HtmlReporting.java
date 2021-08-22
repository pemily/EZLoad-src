package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.sources.Reporting;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Queue;

public class HtmlReporting implements Reporting {
    // https://www.jqueryscript.net/accordion/hierarchical-tree-menu-mg.html
    public static final String END_DOC = "</body>";

    private final Writer writer;
    private final String title;
    private final Queue<Boolean> isLevelInError = new LinkedList<>();
    private boolean headerWritten = false;
    private int levelNumber = 0;

    public HtmlReporting(String title, Writer writer){
        this.title = title;
        this.writer = writer;
    }

    @Override
    public void error(Throwable error) {
        writerHeader();
        StringWriter sw = new StringWriter();
        error.printStackTrace(new PrintWriter(sw));
        write("<span class='error'>"+esc(sw.toString())+"</span>");
        isLevelInError.remove(); // remove the current value for this level
        isLevelInError.offer(true); // and force it to be true
    }

    @Override
    public void error(String error) {
        writerHeader();
        write("<span class='error'>"+esc(error)+"</span>");
        isLevelInError.remove(); // remove the current value for this level
        isLevelInError.offer(true); // and force it to be true
    }

    @Override
    public void info(String info) {
        writerHeader();
        write("<span class='info'>"+esc(info)+"</span>");
    }

    @Override
    public HtmlReporting pushSection(String sectionTitle) {
        writerHeader();
        write("<li><a class='menuitem submenu'>"+ esc(sectionTitle)+"</li><ul>");

        isLevelInError.offer(false);
        levelNumber++;
        return this;
    }

    @Override
    public void popSection() {
        writerHeader();
        levelNumber--;
        Boolean hasError = isLevelInError.poll(); // remove the current value for this level
        if (Boolean.TRUE.equals(hasError))
            write("<script>$(\"this\").parent().addClass('error')</script>");
        write("</ul>");
    }

    private void writerHeader() {
        if (!headerWritten){
            write("<html><head>" +
                    "<link rel='stylesheet' type='text/css' href='file/mgaccordion.css'/>"+
                    "<script type='text/javascript' src='file/mgaccordion.js'></script>"+
                    "<script src='https://code.jquery.com/jquery-3.4.1.min.js'></script>"+
                    "<title>"+esc(title)+"</title>" +
                    "</head><body><div id='menu' class='accordion'><nav class='my-menu'><ul class='my-nav'>");
            headerWritten = true;
        }
    }

    private void write(String text){
        try {
            writer.write(text+"\n");
            writer.flush();
        }
        catch(IOException e){
            throw new BRException(e);
        }
    }

    private String esc(String text){
        return StringEscapeUtils.escapeHtml4(text).replace("\n", "<br>");
    }

    public void end(){
        write("</ul></nav></div>" +
                "<script>$(document).ready(function () { $('.my-nav').mgaccordion(); });</script>" +
                END_DOC);
    }
}
