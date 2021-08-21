package com.pascal.bientotrentier.sources;

import java.io.Closeable;

public interface Reporting extends Closeable {

     Reporting pushSection(String sectionTitle);

     void error(Throwable error);

     void error(String error);

     void info(String info);

     void popSection();

     default void close(){
          popSection();
     }
}
