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