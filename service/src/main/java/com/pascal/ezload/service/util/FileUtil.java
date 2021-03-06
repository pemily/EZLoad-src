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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    public static String file2String(String file) throws FileNotFoundException {
        if (! new File(file).exists()) return null;
        return inputStream2String(new FileInputStream(file));
    }

    public static String inputStream2String(InputStream inputStream) {
        String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
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

    public static void rmdir(File profilDir) throws IOException {
        FileUtils.deleteDirectory(profilDir);
    }

    public static String hashCode(File file) throws IOException {
        return DigestUtils.md5Hex(FileUtils.readFileToByteArray(file));
    }

}
