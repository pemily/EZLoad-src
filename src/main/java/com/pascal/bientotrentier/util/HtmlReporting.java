package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.sources.Reporting;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Queue;

public class HtmlReporting implements Reporting {
    //https://bootsnipp.com/snippets/K3x
    public static final String END_DOC = "</body>";

    private Writer writer;
    private String title;
    private Queue<Boolean> isLevelInError = new LinkedList<>();
    private boolean headerWritten = false;
    private int levelNumber = 0;

    public HtmlReporting(String title, Writer writer){
        this.title = title;
        this.writer = writer;
    }

    @Override
    public HtmlReporting pushSection(String sectionTitle) {
        writerHeader();
        if (levelNumber == 0) write("<h1>"+sectionTitle+"</h1>");
        else write("<details><summary><pre>"+sectionTitle+"</pre></summary>");
        isLevelInError.offer(false);
        levelNumber++;
        return this;
    }

    @Override
    public void error(Throwable error) {
        writerHeader();
        write("<pre class='error'>"+error+"</pre>");
        isLevelInError.remove(); // remove the current value for this level
        isLevelInError.offer(true); // and force it to be true
    }

    @Override
    public void error(String error) {
        writerHeader();
        write("<pre class='error'>"+error+"</pre>");
        isLevelInError.remove(); // remove the current value for this level
        isLevelInError.offer(true); // and force it to be true
    }

    @Override
    public void info(String info) {
        writerHeader();
        write("<pre class='info'>"+info+"</pre>");
    }

    @Override
    public void popSection() {
        writerHeader();
        levelNumber--;
        Boolean hasError = isLevelInError.poll(); // remove the current value for this level
        if (Boolean.TRUE.equals(hasError))
            write("<script>$(\"this\").parent().addClass('error')</script>");
        if (levelNumber != 0)
            write("</details>");
    }

    private void writerHeader() {
        if (!headerWritten){
            write("<html><head>" +
                    "<title>"+title+"</title>" +
                    "<style>" +
                    ".error { color: red; }" +
                    "</style>" +
                    "</head><body>");
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

    public void end(){
        write(END_DOC);
    }
}
