package com.pascal.ezload.service.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    public static String file2String(String file) throws FileNotFoundException {
        String text = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        return text;
    }

    public static void string2file(String file, String content) throws IOException {
        new File(file).getParentFile().mkdirs();
        OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8);
        fileWriter.write(content);
        fileWriter.close();
    }

    public static void unzip(InputStream is, String targetDirStr, boolean fileOverwrite) throws IOException {
        Path targetDir = new File(targetDirStr).toPath();
        try (ZipInputStream zipIn = new ZipInputStream(is)) {
            for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null; ) {
                Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
                if (!resolvedPath.startsWith(targetDir)) {
                    // see: https://snyk.io/research/zip-slip-vulnerability
                    throw new RuntimeException("Entry with an illegal path: "
                            + ze.getName());
                }
                if (ze.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    if (!resolvedPath.toFile().exists() || fileOverwrite)
                        Files.copy(zipIn, resolvedPath);
                }
            }
        }
    }
}
