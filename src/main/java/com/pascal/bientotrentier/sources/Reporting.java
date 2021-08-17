package com.pascal.bientotrentier.sources;

public interface Reporting {

     void pushSection(String sectionTitle);

     void error(Throwable error);

     void error(String error);

     void info(String info);

     void popSection();
}
