package com.pascal.ezload.service.exporter.rules;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.FileProcessor;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RulesManager {

    private final static String RULE_FILE_EXTENSION = ".rule";
    private final static String COMMON_FUNCTIONS_EXTENSION = ".script";

    private final MainSettings mainSettings;
    private final Map<String, CommonFunctions> borkerAndFileVersion2CommonFunctionsCache = new HashMap<>();

    public RulesManager(MainSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public synchronized List<RuleDefinition> getAllRules() throws IOException {
        return new FileProcessor(mainSettings.getEzLoad().getRulesDir(), d -> true, f -> f.getName().endsWith(RULE_FILE_EXTENSION))
                .mapFile(f -> {
                    try {
                        return readRule(f);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public synchronized RuleDefinition readRule(String filepath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        try(Reader reader = new FileReader(filepath)) {
            RuleDefinition ruleDefinition = mapper.readValue(reader, RuleDefinition.class);
            ruleDefinition.validate();
            return ruleDefinition;
        }
    }

    public synchronized String saveRule(String oldName, RuleDefinition ruleDef) throws IOException {
        if (StringUtils.isBlank(ruleDef.getName())) return "Le nom de la règle est vide!";

        String oldFilePath = oldName != null ? getFilePath(oldName, ruleDef) : null;
        String newFilePath = getFilePath(ruleDef.getName().trim(), ruleDef);
        boolean isRenaming = oldFilePath != null && !oldFilePath.equals(newFilePath);

        if (oldName == null){
            // it is a new file
            if (new File(newFilePath).exists()){
                return "Le nom de cette règle existe déjà";
            }
        }
        else{
            if (isRenaming && new File(newFilePath).exists()){
                return "La nom de cette règle existe déjà";
            }
        }

        new File(newFilePath).getParentFile().mkdirs();
        ruleDef.clearErrors();

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        ObjectMapper mapper = new ObjectMapper(jsonFactory).enable(SerializationFeature.INDENT_OUTPUT);;
        mapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(newFilePath), ruleDef);

        if (isRenaming){
            // it is a rename, remove the old file
            boolean ignored = new File(oldFilePath).delete();
        }

        return null;
    }

    private String encodeFile(String filename) {
        return filename.replaceAll("[/\\\\:.?*]", "_");
    }

    private String getFilePath(String name, RuleDefinition ruleDef){
        return mainSettings.getEzLoad().getRulesDir()+File.separator+ruleDef.getBroker().getDirName()+"_v"+ruleDef.getBrokerFileVersion()
                +File.separator+encodeFile(name)+RULE_FILE_EXTENSION;
    }

    public void delete(RuleDefinition ruleDefinition) {
        String filePath = getFilePath(ruleDefinition.getName(), ruleDefinition);
        File f = new File(filePath);
        if (f.exists()){
            f.delete();
        }
    }

    private String getCommonFilePath(EnumEZBroker broker, int borkerFileVersion){
        return mainSettings.getEzLoad().getRulesDir()+File.separator+broker.getDirName()+"_v"+borkerFileVersion+COMMON_FUNCTIONS_EXTENSION;
    }

    public synchronized CommonFunctions getCommonScript(RuleDefinition ruleDef){
        return borkerAndFileVersion2CommonFunctionsCache.computeIfAbsent(ruleDef.getBroker().getDirName()+ruleDef.getBrokerFileVersion(),
                (key) -> {
                    try {
                        return readCommonScript(ruleDef.getBroker(), ruleDef.getBrokerFileVersion());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public synchronized CommonFunctions readCommonScript(EnumEZBroker broker, int brokerFileVersion) throws IOException {
        String commonFile = getCommonFilePath(broker, brokerFileVersion);
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        if (new File(commonFile).exists()) {
            try (Reader reader = new FileReader(commonFile)) {
                CommonFunctions content = mapper.readValue(reader, CommonFunctions.class);
                return content;
            }
        }
        else{
            CommonFunctions commonFunctions = new CommonFunctions();
            commonFunctions.setBrokerFileVersion(brokerFileVersion);
            commonFunctions.setBroker(broker);
            commonFunctions.setScript("// Liste de fonctions utilisables dans toutes les expressions de "+broker+" v"+brokerFileVersion
                    +"\n\n\n");
            return commonFunctions;
        }
    }

    public synchronized CommonFunctions saveCommonScript(CommonFunctions function) throws IOException {
        String commonFile = getCommonFilePath(function.getBroker(), function.getBrokerFileVersion());
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        ObjectMapper mapper = new ObjectMapper(jsonFactory).enable(SerializationFeature.INDENT_OUTPUT);;
        mapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(commonFile), function);
        borkerAndFileVersion2CommonFunctionsCache.put(function.getBroker().getDirName()+function.getBrokerFileVersion(), function);
        return function;
    }
}
