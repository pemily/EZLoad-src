package com.pascal.ezload.service.util;

import java.io.IOException;
import java.io.Writer;

public class MultiWriter extends Writer {

    private final Writer[] writers;

    public MultiWriter(Writer... writers) {
        this.writers = writers;
    }

    @Override
    public void write(char[] cbuf, int off, int len){
        for (Writer w : writers) {
            try {
                w.write(cbuf, off, len);
            }
            catch (IOException e){
                // Ignore exception if the log file cannot be writter (html page is perhaps lost)
            }
        }
    }

    @Override
    public void flush() {
        for (Writer w : writers) {
            try {
                w.flush();
            }
            catch (IOException e){
                // Ignore exception if the log file cannot be writter (html page is perhaps lost)
            }
        }
    }

    @Override
    public void close() {
        for (Writer w : writers) {
            try {
                w.close();
            }
            catch (IOException e){
                // Ignore exception if the log file cannot be writter (html page is perhaps lost)
            }
        }
    }

};