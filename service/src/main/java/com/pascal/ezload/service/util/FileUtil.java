package com.pascal.ezload.service.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

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
}
