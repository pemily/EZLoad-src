package com.pascal.ezload.service.exporter.rules;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.rules.update.RulesVersionManager;
import com.pascal.ezload.service.util.FileProcessor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RulesManager {

    private final static String RULE_FILE_EXTENSION = ".rule";
    private final static String COMMON_FUNCTIONS_FILENAME = "common.script";
    private final static DefaultIndenter defaultIndenter = new DefaultIndenter("  ", "\n"); // tab with 2 spaces and \n instead of \n\r

    private final RulesVersionManager rulesVersionManager;
    private final MainSettings mainSettings;
    private final Map<String, CommonFunctions> brokerAndFileVersion2CommonFunctionsCache = new HashMap<>();

    public RulesManager(String ezRepoDir, MainSettings mainSettings) {
        this.mainSettings = mainSettings;
        this.rulesVersionManager = new RulesVersionManager(ezRepoDir, mainSettings);
    }

    public synchronized List<RuleDefinition> getAllRules() throws IOException {
        return new FileProcessor(mainSettings.getEzLoad().getRulesDir(), d -> true, f -> f.getName().endsWith(RULE_FILE_EXTENSION))
                .mapFile(f -> {
                    try {
                        RulesVersionManager.FileState state = rulesVersionManager.getState(f);
                        return readRule(f, state == RulesVersionManager.FileState.NEW,
                                    state != RulesVersionManager.FileState.NEW && state != RulesVersionManager.FileState.NO_CHANGE);
                    } catch (IOException | GitAPIException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    public synchronized RuleDefinition readRule(String filepath, boolean newUserRule, boolean dirtyFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        try(Reader reader = new FileReader(filepath, StandardCharsets.UTF_8)) {
            RuleDefinition ruleDefinition = mapper.readValue(reader, RuleDefinition.class);
            ruleDefinition.validate();
            ruleDefinition.setNewUserRule(newUserRule);
            ruleDefinition.setDirtyFile(dirtyFile);
            return ruleDefinition;
        }
    }

    // si le nom de la regle ne change pas, oldName = null
    public synchronized String saveRule(String oldName, RuleDefinition ruleDef) throws IOException {
        if (StringUtils.isBlank(ruleDef.getName())) return "Le nom de la règle est vide!";

        String oldFilePath = oldName != null ? getRuleFilePath(oldName, ruleDef.getBroker(), ruleDef.getBrokerFileVersion()) : null;
        String newFilePath = getRuleFilePath(ruleDef.getName().trim(), ruleDef.getBroker(), ruleDef.getBrokerFileVersion());
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


    public void delete(RuleDefinition ruleDefinition) throws GitAPIException, IOException {
        String filePath = getRuleFilePath(ruleDefinition.getName(), ruleDefinition.getBroker(), ruleDefinition.getBrokerFileVersion());
        File f = new File(filePath);
        if (ruleDefinition.isNewUserRule() && f.exists()){
            f.delete();
        }
        else{
            if (ruleDefinition.isDirtyFile()){
                rulesVersionManager.revert(f.getAbsolutePath());
            }
        }
    }

    public String getRulesDirectory(EnumEZBroker broker, int brokerFileVersion){
        return mainSettings.getEzLoad().getRulesDir()+File.separator+File.separator+broker.getDirName()+"_v"+brokerFileVersion;
    }

    public String getFile(String filename, EnumEZBroker broker, int brokerFileVersion){
        return getRulesDirectory(broker, brokerFileVersion)+File.separator+filename;
    }

    private String encodeFile(String filename) {
        return filename.replaceAll("[/\\\\:.?*]", "_");
    }

    private String getRuleFilePath(String name, EnumEZBroker broker, int brokerFileVersion){
        return getFile(encodeFile(name)+RULE_FILE_EXTENSION, broker, brokerFileVersion);
    }

    private String getCommonFilePath(EnumEZBroker broker, int brokerFileVersion){
        return getFile(COMMON_FUNCTIONS_FILENAME, broker, brokerFileVersion);
    }

    public synchronized CommonFunctions getCommonScript(EnumEZBroker broker, int brokerFileVersion){
        return brokerAndFileVersion2CommonFunctionsCache.computeIfAbsent(broker.getDirName()+brokerFileVersion,
                (key) -> {
                    try {
                        return readCommonScript(broker, brokerFileVersion, rulesVersionManager.getState(getCommonFilePath(broker, brokerFileVersion)) != RulesVersionManager.FileState.NO_CHANGE);
                    } catch (IOException | GitAPIException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private synchronized CommonFunctions readCommonScript(EnumEZBroker broker, int brokerFileVersion, boolean dirtyFile) throws IOException {
        String commonFile = getCommonFilePath(broker, brokerFileVersion);
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        if (new File(commonFile).exists()) {
            try (Reader reader = new FileReader(commonFile)) {
                CommonFunctions content = mapper.readValue(reader, CommonFunctions.class);
                content.setDirtyFile(dirtyFile);
                return content;
            }
        }
        else{
            CommonFunctions commonFunctions = new CommonFunctions();
            commonFunctions.setBrokerFileVersion(brokerFileVersion);
            commonFunctions.setDirtyFile(dirtyFile);
            commonFunctions.setBroker(broker);
            commonFunctions.setScript(("// Liste de fonctions utilisables dans toutes les expressions de "+broker+" v"+brokerFileVersion
                    +"\n\n\n").split("\n"));
            return commonFunctions;
        }
    }

    public synchronized CommonFunctions saveCommonScript(CommonFunctions function) throws IOException {
        String commonFile = getCommonFilePath(function.getBroker(), function.getBrokerFileVersion());

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
