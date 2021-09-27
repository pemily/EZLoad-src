package com.pascal.ezload.service.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Writer;

public class Tail {

    public static void tail(File file, Writer writer, String startFromSentence, String stopSentence) {
        boolean running = true;
        int updateInterval = 200;
        long filePointer = 0; // If I want to skip n char from the start of the file
        boolean startSentenceDetected = false;

        try {
            while (running) {
                Sleep.waitMillisecs(updateInterval);
                long length = file.length();
                if (length < filePointer) {
                    // Log file was reset. Restarting logging from start of file.
                    filePointer = length;
                } else if (length > filePointer) {
                    RandomAccessFile localRandomAccessFile = new RandomAccessFile(file, "r");
                    localRandomAccessFile.seek(filePointer);
                    String str;
                    while ((str = localRandomAccessFile.readLine()) != null) {
                        if (!startSentenceDetected){
                          if (str.equals(startFromSentence)) startSentenceDetected = true;
                          continue;
                        }
                        if (str.equals(stopSentence)){
                            running = false;
                            break;
                        }
                        writer.write(str);
                        writer.flush();
                    }
                    filePointer = localRandomAccessFile.getFilePointer();
                    localRandomAccessFile.close();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
