package com.pascal.ezload.service.rules.update;

import com.google.api.client.json.gson.GsonFactory;
import com.pascal.ezload.github.api.ReposApi;
import com.pascal.ezload.github.handler.ApiClient;
import com.pascal.ezload.github.handler.ApiException;
import com.pascal.ezload.github.model.ContentTree;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.HttpUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Github {
    private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    private final String owner;
    private final String repo;
    private final String branch;

    public Github(String owner, String repo, String branch) {
        this.owner = owner;
        this.repo = repo;
        this.branch = branch;
    }

    private ApiClient getClient(){
        return new ApiClient();
    }

    // https://api.github.com/repos/pemily/EZLoad-Rules/contents/rules/BourseDirect_v1?ref=EZLoad-1.0 <= j'ai essayé avec l'api de Github, mais le type de retour marche pas, le code attend un objet, on recoit une liste, bug dans la generation? ou bug du yaml?
    // https://api.github.com/repos/pemily/EZLoad-Rules/git/trees/EZLoad-1.0?recursive=true
    public List<RemoteFile> getOfficialFiles(EnumEZBroker broker, int brokerFileVersion) throws ApiException, IOException {
        String path = "rules/"+broker.getDirName()+"_v"+brokerFileVersion;
        String content = HttpUtil.urlContent("https://api.github.com/repos/"+owner+"/"+repo+"/contents/"+path+"?ref="+branch);

        List<Map<String, Object>> allFiles = (List<Map<String, Object>>) gsonFactory.fromString(content, ArrayList.class);

        return allFiles
                .stream()
                .filter(f -> f.get("type").equals("file"))
                .map(f -> new RemoteFile(f.get("name").toString(), broker, brokerFileVersion, f.get("download_url").toString())).collect(Collectors.toList());
    }

}
