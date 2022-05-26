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
package com.pascal.ezload.service.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ZipFileCompressUtils {

    public String extractOneFile(String zipFilePath, String extractDirectory) throws Exception {
        try {
            Path filePath = Paths.get(zipFilePath);
            try(ArchiveInputStream archiveInputStream = new ArchiveStreamFactory()
                    .createArchiveInputStream(ArchiveStreamFactory.ZIP, Files.newInputStream(filePath)))
                {
                    ArchiveEntry archiveEntry;
                    while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
                        Path path = Paths.get(extractDirectory, archiveEntry.getName());
                        File file = path.toFile();
                        if (archiveEntry.isDirectory()) {
                            if (!file.isDirectory()) {
                                file.mkdirs();
                            }
                        } else {
                            File parent = file.getParentFile();
                            if (!parent.isDirectory()) {
                                parent.mkdirs();
                            }
                            try (OutputStream outputStream = Files.newOutputStream(path)) {
                                IOUtils.copy(archiveInputStream, outputStream);
                            }
                            catch(Exception e){
                                // do nothing, we didn't success to extract it, perhaps it is locked by the system
                                // not a problem, we have the file, we can go ahead for the next stage
                            }
                            return file.getAbsolutePath();
                        }
                    }
                }
        } catch (Exception e){
            throw e;
        }
        return null;
    }

    public void extractZip(String zipFilePath, String extractDirectory) throws Exception {
        try {
            Path filePath = Paths.get(zipFilePath);
            try(ArchiveInputStream archiveInputStream = new ArchiveStreamFactory()
                    .createArchiveInputStream(ArchiveStreamFactory.ZIP, Files.newInputStream(filePath))) {
                ArchiveEntry archiveEntry;
                while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
                    Path path = Paths.get(extractDirectory, archiveEntry.getName());
                    File file = path.toFile();
                    if (archiveEntry.isDirectory()) {
                        if (!file.isDirectory()) {
                            file.mkdirs();
                        }
                    } else {
                        File parent = file.getParentFile();
                        if (!parent.isDirectory()) {
                            parent.mkdirs();
                        }
                        try (OutputStream outputStream = Files.newOutputStream(path)) {
                            IOUtils.copy(archiveInputStream, outputStream);
                        }
                    }
                }
            }
        } catch (Exception e){
            throw e;
        }
    }
}