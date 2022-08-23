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
