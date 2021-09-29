package com.pascal.ezload.service.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpUtil {

    public static String urlContent(String url) throws IOException {
        return IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
    }

    public static void download(String url, File output) throws IOException{
        IOUtils.copy(new URL(url), output);
    }
}
