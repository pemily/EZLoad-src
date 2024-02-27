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
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.pascal.ezload.service.sources.Reporting;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

public class HttpUtilCached {

    private String cacheDir;

    public HttpUtilCached(String cacheDir){
        this.cacheDir = cacheDir;
    }

    public <R> R get(Reporting reporting, String cacheName, String url, FunctionThatThrow<InputStream, R> toObjMapper) throws Exception {
        return get(reporting, cacheName, url, ( Map<String, String>)null, toObjMapper);
    }

    public <R> R get(Reporting reporting, String cacheName, String url, Map<String, String> requestProperties, FunctionThatThrow<InputStream, R> toObjMapper) throws Exception {
        return get(reporting, cacheName, url, () -> {
                    Page page = HttpUtil.getFromUrl(url, false);
                    if (page.getWebResponse().isSuccess()) {
                        if (page instanceof UnexpectedPage) {
                            return ((UnexpectedPage) page).getInputStream();
                        } else if (page instanceof TextPage) {
                            return new ByteArrayInputStream(((TextPage) page).getContent().getBytes(StandardCharsets.UTF_8));
                        } else if (page instanceof HtmlPage) {
                            throw new RuntimeException("Html Page don't know what to do");
                        }
                        else {
                            throw new RuntimeException("TEST SI ON RECOIT un autre type de page: " + page.getClass().getSimpleName() + " url: " + url);
                        }
                    } else {
                        return HttpUtil.downloadV2(url, null, inputStream -> inputStream);
                    }
                }, toObjMapper);
    }



    public <R> R get(Reporting reporting, String cacheName, String url, SupplierThatThrow<InputStream> noCacheFunction, FunctionThatThrow<InputStream, R> toObjMapper) throws Exception {
        try(Reporting rep = reporting.pushSection("Recherche de "+cacheName+" url: "+url)) {
            File cache = new File(cacheDir + File.separator + format(cacheName) + ".json");
            if (cache.exists()) {
                rep.info("Fichier de cache trouvé: "+cache.getAbsolutePath());
                try (InputStream in = FileUtil.read(cache)) {
                    return toObjMapper.apply(in);
                }
            }
            rep.info("Fichier de cache non trouvé, téléchargement des données");
            try (InputStream in = noCacheFunction.get()){
                FileUtil.write(cache, in);
            }
            rep.info("Fin de téléchargement");
            try (InputStream in = FileUtil.read(cache)) {
                return toObjMapper.apply(in);
            }
        }
    }

    private String format(String cacheName) {
        cacheName = cacheName.replaceAll("[*?:/\\\\]", "_");
        return cacheName;
    }

    public boolean exists(String cacheName){
        File cache = new File(cacheDir+File.separator+format(cacheName)+".json");
        return cache.exists();
    }

    public void createCache(String cacheName, String content) throws IOException {
        FileUtil.string2file(cacheDir+File.separator+format(cacheName)+".json", content);

    }

    public InputStream getInputStream(String cacheName) throws FileNotFoundException {
        File cache = new File(cacheDir+File.separator+format(cacheName)+".json");
        return FileUtil.read(cache);
    }


    public String getContent(String cacheName) throws IOException {
        return FileUtil.file2String(cacheDir+File.separator+format(cacheName)+".json");
    }

}
