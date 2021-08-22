package com.pascal.bientotrentier.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Writer;

public class Tail {

    public static void tail(File file, Writer writer, boolean startFromBegining, String stopSentence) {
        boolean running = true;
        int updateInterval = 500;
        long filePointer = startFromBegining ? 0 : file.length();

        try {
            while (running) {
                Thread.sleep(updateInterval);
                long length = file.length();
                if (length < filePointer) {
                    // Log file was reset. Restarting logging from start of file.
                    filePointer = length;
                } else if (length > filePointer) {
                    RandomAccessFile localRandomAccessFile = new RandomAccessFile(file, "r");
                    localRandomAccessFile.seek(filePointer);
                    String str;
                    while ((str = localRandomAccessFile.readLine()) != null) {
                        writer.write(str);
                        writer.flush();
                        if (stopSentence != null && str.contains(stopSentence)){
                            running = false;
                        }
                    }
                    filePointer = localRandomAccessFile.getFilePointer();
                    localRandomAccessFile.close();
                }
            }
        } catch (Exception ex) {
        }
    }
}
