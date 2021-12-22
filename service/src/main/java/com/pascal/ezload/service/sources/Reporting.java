package com.pascal.ezload.service.sources;

import com.pascal.ezload.service.util.TitleWithFileRef;

import java.io.Closeable;
import java.io.IOException;

public interface Reporting extends Closeable {

     Reporting pushSection(String sectionTitle);
     Reporting pushSection(TitleWithFileRef sectionTitle);

     void error(Throwable error);

     void error(String error);

     default void error(String msg, Throwable error){
          error(msg);
          error(error);
     }

     void info(String info);

     void popSection();

     String escape(String text);

     default void close() throws IOException {
          popSection();
     }
}
