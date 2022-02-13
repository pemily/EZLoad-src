package com.pascal.ezload.service.exporter.rules;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.FileProcessor;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RulesManager {

    private final static String RULE_FILE_EXTENSION = ".rule";
    private final static String COMMON_FUNCTIONS_FILENAME = "common.script";
    private final static DefaultIndenter defaultIndenter = new DefaultIndenter("  ", "\n"); // tab with 2 spaces and \n instead of \n\r

    private final MainSettings mainSettings;
    private final Map<String, CommonFunctions> brokerAndFileVersion2CommonFunctionsCache = new HashMap<>();

    public RulesManager(MainSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public synchronized List<RuleDefinition> getAllRules() throws IOException {
        List<RuleDefinition> userRuleDefinitions = new FileProcessor(mainSettings.getEzLoad().getRulesDir()+File.separator+SettingsManager.RULE_LOCAL_DIR, d -> true, f -> f.getName().endsWith(RULE_FILE_EXTENSION))
                .mapFile(f -> {
                    try {
                        return readRule(f, true, false); // sharedVersionExists => false for the moment, will be changed below if the shared version exists
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        // merge les 2 listes et ecrase les shared par user si besoin
        List<RuleDefinition> sharedRuleDefinitionsFiltered = new FileProcessor(mainSettings.getEzLoad().getRulesDir()+File.separator+SettingsManager.RULE_SHARED_DIR, d -> true, f -> f.getName().endsWith(RULE_FILE_EXTENSION))
                .mapFile(f -> {
                    try {
                        RuleDefinition sharedRuleDefinition = readRule(f, false, true); // sharedVersionExists == true, since I'm reading the shared directory
                        Optional<RuleDefinition> rd = userRuleDefinitions.stream()
                                                        .filter(userRule -> userRule.getName().equals(sharedRuleDefinition.getName())
                                                            && userRule.getBroker() == sharedRuleDefinition.getBroker()
                                                            && userRule.getBrokerFileVersion() == sharedRuleDefinition.getBrokerFileVersion())
                                                        .findFirst();
                        if (rd.isPresent()) {
                            rd.get().setSharedVersionExists(true); // I detect now, that the shared rule definition is overriden by the user
                            return null; // return null if a userRule already exists with the same name for the same broker & broker file version
                        }
                        return sharedRuleDefinition;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        // merge both list
        userRuleDefinitions.addAll(sharedRuleDefinitionsFiltered);
        return userRuleDefinitions; // the final list with only the shared rules that are not overriden + the user rules
    }


    public synchronized RuleDefinition readRule(String filepath, boolean userRule, boolean sharedVersionExists) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        try(Reader reader = new FileReader(filepath, StandardCharsets.UTF_8)) {
            RuleDefinition ruleDefinition = mapper.readValue(reader, RuleDefinition.class);
            ruleDefinition.validate();
            ruleDefinition.setUserRule(userRule);
            ruleDefinition.setSharedVersionExists(sharedVersionExists);
            return ruleDefinition;
        }
    }

    // si le nom de la regle ne change pas, oldName = null
    public synchronized String saveRule(String oldName, RuleDefinition ruleDef) throws IOException {
        if (StringUtils.isBlank(ruleDef.getName())) return "Le nom de la règle est vide!";

        String oldFilePath = oldName != null ? getRuleFilePath(oldName, ruleDef.getBroker(), ruleDef.getBrokerFileVersion(), true) : null;
        String newFilePath = getRuleFilePath(ruleDef.getName().trim(), ruleDef.getBroker(), ruleDef.getBrokerFileVersion(), true);
        boolean isRenaming = oldFilePath != null && !oldFilePath.equals(newFilePath);

        if (oldName == null){
            // it is a new file
            if (new File(newFilePath).exists()){
                return "Le nom de cette règle existe déjà";
            }
        }
        else{
            if (isRenaming && new File(newFilePath).exists()){
                return "Le nom de cette règle existe déjà";
            }
        }

        new File(newFilePath).getParentFile().mkdirs();
        ruleDef.clearErrors();
        ruleDef.beforeSave(RulesManager::normalize);

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        ObjectMapper mapper = new ObjectMapper(jsonFactory).enable(SerializationFeature.INDENT_OUTPUT);;

        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(defaultIndenter);
        prettyPrinter.indentObjectsWith(defaultIndenter);
        prettyPrinter.withArrayIndenter(defaultIndenter);
        prettyPrinter.withObjectIndenter(defaultIndenter);

        mapper.writer(prettyPrinter).writeValue(new FileWriter(newFilePath, StandardCharsets.UTF_8), ruleDef);

        if (isRenaming){
            // it is a rename, remove the old file
            boolean ignored = new File(oldFilePath).delete();
        }

        return null;
    }


    public void delete(RuleDefinition ruleDefinition) {
        String filePath = getRuleFilePath(ruleDefinition.getName(), ruleDefinition.getBroker(), ruleDefinition.getBrokerFileVersion(), true);
        File f = new File(filePath);
        if (f.exists()){
            f.delete();
        }
    }

    public String getRulesDirectory(EnumEZBroker broker, int brokerFileVersion, boolean userRule){
        return mainSettings.getEzLoad().getRulesDir()+File.separator
                +(userRule ? SettingsManager.RULE_LOCAL_DIR : SettingsManager.RULE_SHARED_DIR)
                +File.separator+broker.getDirName()+"_v"+brokerFileVersion;
    }

    public String getFile(String filename, EnumEZBroker broker, int brokerFileVersion, boolean userRule){
        return getRulesDirectory(broker, brokerFileVersion, userRule)+File.separator+filename;
    }

    private String encodeFile(String filename) {
        return filename.replaceAll("[/\\\\:.?*]", "_");
    }

    private String getRuleFilePath(String name, EnumEZBroker broker, int brokerFileVersion, boolean userRule){
        return getFile(encodeFile(name)+RULE_FILE_EXTENSION, broker, brokerFileVersion, userRule);
    }

    private String getCommonFilePath(EnumEZBroker broker, int brokerFileVersion, boolean userRule){
        return getFile(COMMON_FUNCTIONS_FILENAME, broker, brokerFileVersion, userRule);
    }

    public synchronized CommonFunctions getCommonScript(EnumEZBroker broker, int brokerFileVersion){
        return brokerAndFileVersion2CommonFunctionsCache.computeIfAbsent(broker.getDirName()+brokerFileVersion,
                (key) -> {
                    try {
                        // get the user rule if it exists
                        boolean userRule = new File(getCommonFilePath(broker, brokerFileVersion, true)).exists();
                        boolean sharedVersionExists = new File(getCommonFilePath(broker, brokerFileVersion, false)).exists();
                        return readCommonScript(broker, brokerFileVersion, userRule, sharedVersionExists);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private synchronized CommonFunctions readCommonScript(EnumEZBroker broker, int brokerFileVersion, boolean userRule, boolean sharedVersionExists) throws IOException {
        String commonFile = getCommonFilePath(broker, brokerFileVersion, userRule);
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        if (new File(commonFile).exists()) {
            try (Reader reader = new FileReader(commonFile)) {
                CommonFunctions content = mapper.readValue(reader, CommonFunctions.class);
                content.setUserRule(userRule);
                content.setSharedVersionExists(sharedVersionExists);
                return content;
            }
        }
        else{
            CommonFunctions commonFunctions = new CommonFunctions();
            commonFunctions.setBrokerFileVersion(brokerFileVersion);
            commonFunctions.setUserRule(userRule);
            commonFunctions.setSharedVersionExists(sharedVersionExists);
            commonFunctions.setBroker(broker);
            commonFunctions.setScript(("// Liste de fonctions utilisables dans toutes les expressions de "+broker+" v"+brokerFileVersion
                    +"\n\n\n").split("\n"));
            return commonFunctions;
        }
    }

    public synchronized CommonFunctions saveCommonScript(CommonFunctions function) throws IOException {
        String commonFile = getCommonFilePath(function.getBroker(), function.getBrokerFileVersion(), true);

        // to ease the comparison in github
        function.setScript(Arrays.stream(function.getScript()).map(RulesManager::normalize).collect(Collectors.toList()).toArray(new String[]{}));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

        ObjectMapper mapper = new ObjectMapper(jsonFactory)
                .enable(SerializationFeature.INDENT_OUTPUT);

        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(defaultIndenter);

        mapper.writer(prettyPrinter).writeValue(new FileWriter(commonFile, StandardCharsets.UTF_8), function);

        brokerAndFileVersion2CommonFunctionsCache.put(function.getBroker().getDirName()+function.getBrokerFileVersion(), function);
        return function;
    }

    public static String normalize(String line) {
        line = line.replace("\t", "    "); // replace tabulation by 4 spaces
        line = line.replace("\"", "'"); // replace " by '
        line = line.trim();
        return line;
    }

}
