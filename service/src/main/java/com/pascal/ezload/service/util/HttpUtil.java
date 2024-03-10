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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private static final HttpClient httpClient;

    static {
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2) // Spécifie la version HTTP
                .connectTimeout(Duration.ofSeconds(10));
        httpClientBuilder = httpClientBuilder.executor(java.util.concurrent.Executors.newFixedThreadPool(1));
        httpClient = httpClientBuilder.build();
    }

    public static String urlContent(String url) throws IOException {
        return IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
    }

    public static void download(String url, File output) throws IOException{
        IOUtils.copy(new URL(url), output);
    }

    public static <R> R downloadV2(String urlStr, List<String[]> requestProperties, FunctionThatThrow<InputStream, R> f) throws Exception {
        HttpResponse<InputStream> response = httpGET(urlStr, requestProperties);
        if (response.statusCode() == 302 || response.statusCode() == 303){
            // redirect
            String newLocation = response.headers().firstValue("location").orElse(null);
            if (newLocation != null){
                return downloadV2(newLocation, requestProperties, f);
            }
            else{
                throw new RuntimeException("302 sur url: "+urlStr+" headers: "+response.headers().map());
            }
        }
        try(InputStream in =response.body()) {
            if (response.statusCode() / 100 != 2){
                throw new DownloadException("Impossible to download data at "+urlStr+" status code:"+response.statusCode()+" \n"+IOUtils.toString(in, StandardCharsets.UTF_8));
            }
            return f.apply(in);
        }
    }

    public static HttpResponse<InputStream> httpGET(String urlStr, List<String[]> requestProperties) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_2) // Spécifie la version HTTP
                .GET()
                .uri(URI.create(urlStr));

        if (requestProperties != null) requestProperties.forEach(h -> requestBuilder.header(h[0], h[1]));

        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
    }

    public static <R> R download(String urlStr, Map<String, String> requestProperties, FunctionThatThrow<InputStream, R> f) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setUseCaches(false);
            con.setDefaultUseCaches(false);
            con.setInstanceFollowRedirects(false);
            con.setAllowUserInteraction(false);
            con.setDoInput(false);
            con.setDoOutput(false);
            if (requestProperties != null) {
                requestProperties
                        .forEach(con::addRequestProperty);

            }
            con.addRequestProperty("HOST", url.getHost());
            con.setRequestMethod("GET");
            R r = null;
            try (InputStream input = new BufferedInputStream(con.getInputStream())){
                r = f.apply(input);
            }
            return r;
        } finally {
            con.disconnect();
        }
    }

    public static Page getFromUrl(String url, boolean enableJavascript) throws IOException {
        try (final WebClient webClient = new WebClient()) {
            // Ignorer les avertissements et les erreurs JavaScript, si nécessaire
            webClient.getOptions().setJavaScriptEnabled(enableJavascript);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getCookieManager().setCookiesEnabled(true);
            webClient.getOptions().setPopupBlockerEnabled(true);
            webClient.getOptions().setFetchPolyfillEnabled(true);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);


            // Récupérer la page HTML
            return webClient.getPage(url);
        }
    }


    public static List<String[]> chromeHeader() {
        List<String[]> header= new LinkedList<>();

        header.add(new String[]{ "sec-ch-ua","\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\""});
        header.add(new String[]{ "sec-ch-ua-mobile","?0"});
        header.add(new String[]{ "sec-ch-ua-platform", "\"Windows\""});
        header.add(new String[]{ "upgrade-insecure-requests","1"});
        header.add(new String[]{ "user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"});
        header.add(new String[]{ "accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"});
        header.add(new String[]{ "sec-fetch-site","none"});
        header.add(new String[]{ "sec-fetch-mode","navigate"});
        header.add(new String[]{ "sec-fetch-user","?1"});
        header.add(new String[]{ "sec-fetch-dest","document"});
        header.add(new String[]{ "accept-encoding","gzip, deflate, br"});
        header.add(new String[]{ "accept-language","fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7"});
        return header;
    }


    private static int userAgentIndex = 0;
    public static String getUserAgent(){
        String[] list = new String []{
                // https://deviceatlas.com/blog/list-of-user-agent-strings
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36,gzip(gfe)",
                "Mozilla/5.0 (Linux; Android 13; SM-S901B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 13; SM-S901U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.83 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:98.0) Gecko/20100101 Firefox/98.0",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Linux; Android 13; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 13; SM-S908U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 13; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 12; moto g pure) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 12; Redmi Note 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36",
                "Mozilla/5.0 (iPhone14,3; U; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) Version/10.0 Mobile/19A346 Safari/602.1",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/69.0.3497.105 Mobile/15E148 Safari/605.1",
                "Mozilla/5.0 (Windows Phone 10.0; Android 6.0.1; Microsoft; RM-1152) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Mobile Safari/537.36 Edge/15.15254",
                "Mozilla/5.0 (Linux; Android 12; SM-X906C Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/80.0.3987.119 Mobile Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36",
                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/601.3.9 (KHTML, like Gecko) Version/9.0.2 Safari/601.3.9",
                "Mozilla/5.0 (PlayStation; PlayStation 5/2.26) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Safari/605.1.15",
        };

        if (++userAgentIndex == list.length) userAgentIndex = 0;
        return list[userAgentIndex];
    }


    public static class DownloadException extends Exception {
        public DownloadException(String msg){
            super(msg);
        }
    }
}
