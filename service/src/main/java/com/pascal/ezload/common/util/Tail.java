/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.common.util;

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
