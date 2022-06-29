package com.pascal.ezload.service.sources.jsonSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.JsonUtil;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class JsonSource {

    public static EZModel genModelFromJson(Reporting reporting, EzProfil ezProfil, EZAccountDeclaration ezAccountDeclaration, String jsonFilePath) throws IOException {
        EZModel model = genModelFromJson(jsonFilePath);
        model.setAccountDeclaration(ezAccountDeclaration);
        model.setSourceFile(ezProfil.getSourceRef(jsonFilePath));
        model.getOperations().forEach(op -> {
            op.setEzAccountDeclaration(ezAccountDeclaration);
            op.setAccount(model.getAccount());
            op.setBroker(model.getBroker());
        });
        return model;
    }



    public static EZModel genModelFromJson(String filepath) throws IOException {
        try(Reader reader = new FileReader(filepath, StandardCharsets.UTF_8)) {
            ObjectMapper mapper = JsonUtil.createDefaultMapper();
            return mapper.readValue(reader, EZModel.class);
        }
    }

}
