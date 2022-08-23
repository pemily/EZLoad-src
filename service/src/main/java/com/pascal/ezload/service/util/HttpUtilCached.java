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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class HttpUtilCached {

    private String cacheDir;

    public HttpUtilCached(String cacheDir){
        this.cacheDir = cacheDir;
    }

    public <R> R get(String cacheName, String url, FunctionThatThrow<InputStream, R> toObjMapper) throws Exception {
        return get(cacheName, url, null, toObjMapper);
    }

    public <R> R get(String cacheName, String url, Map<String, String> requestProperties, FunctionThatThrow<InputStream, R> toObjMapper) throws Exception {
        File cache = new File(cacheDir+File.separator+format(cacheName)+".json");
        if (cache.exists()){
            return toObjMapper.apply(FileUtil.read(cache));
        }
        HttpUtil.download(url, requestProperties, inputStream -> {
            FileUtil.write(cache, inputStream);
            return cache;
        });
        return toObjMapper.apply(FileUtil.read(cache));
    }

    private String format(String cacheName) {
        cacheName = cacheName.replaceAll("[*?:/\\\\]", "_");
        return cacheName;
    }

    public boolean exists(String cacheName){
        File cache = new File(cacheDir+File.separator+format(cacheName)+".json");
        return cache.exists();
    }

    public InputStream getInputStream(String cacheName) throws FileNotFoundException {
        File cache = new File(cacheDir+File.separator+format(cacheName)+".json");
        return FileUtil.read(cache);
    }
}
