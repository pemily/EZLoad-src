package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.sources.Reporting;

public class TextReporting implements Reporting {

    private static String SECTION_TAB = "    ";
    private StringBuilder report = new StringBuilder();

    private String tab = "";

    public void pushSection(String sectionTitle) {
        report.append(tab).append("===== "+sectionTitle+"\n");
        tab+=SECTION_TAB;
    }

    public void error(Throwable error){
        report.append(tab).append("Exception: "+error.getMessage()+"\n");
    }

    public void error(String error){
        report.append(tab).append("error: "+error+"\n");
    }

    public void info(String info){
        report.append(tab).append("info: "+info+"\n");
    }

    public void popSection() {
        tab=tab.substring(SECTION_TAB.length());
        report.append(tab).append("=====\n");

    }

    public String getReport() {
        return report.toString();
    }
}
