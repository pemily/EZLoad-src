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

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HttpUtil {

    public static String urlContent(String url) throws IOException {
        return IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
    }

    public static void download(String url, File output) throws IOException{
        IOUtils.copy(new URL(url), output);
    }


    public static <R> R download(String urlStr, Map<String, String> requestProperties, FunctionThatThrow<InputStream, R> f) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            // con.setUseCaches(false);
            if (requestProperties != null) {
                requestProperties
                        .forEach(con::addRequestProperty);

            }
            con.addRequestProperty("HOST", url.getHost());
            con.setRequestMethod("GET");
            InputStream input = new BufferedInputStream(con.getInputStream());
            return f.apply(input);
        } finally {
            con.disconnect();
        }
    }
}
