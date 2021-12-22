package com.pascal.ezload.service.util;

import com.pascal.ezload.service.sources.Reporting;

import java.io.IOException;

public class TextReporting implements Reporting {

    private static String SECTION_TAB = "    ";
    private StringBuilder report = new StringBuilder();

    private String tab = "";

    @Override
    public Reporting pushSection(String sectionTitle) {
        report.append(tab).append("===== "+sectionTitle+"\n");
        tab+=SECTION_TAB;
        return this;
    }

    @Override
    public Reporting pushSection(TitleWithFileRef sectionTitle) {
        return pushSection(sectionTitle.format(this, Reporting::escape));
    }

    @Override
    public void error(Throwable error){
        report.append(tab).append("Exception: "+error.getMessage()+"\n");
    }

    @Override
    public void error(String error){
        report.append(tab).append("error: "+error+"\n");
    }

    @Override
    public void info(String info){
        report.append(tab).append("info: "+info+"\n");
    }

    @Override
    public void popSection() {
        tab=tab.substring(SECTION_TAB.length());
        report.append(tab).append("=====\n");

    }

    @Override
    public String escape(String text) {
        return text;
    }

    public String getReport() {
        return report.toString();
    }

}
