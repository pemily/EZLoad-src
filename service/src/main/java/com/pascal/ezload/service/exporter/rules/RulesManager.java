package com.pascal.ezload.service.exporter.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.sources.FileProcessor;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

public class RulesManager {

    private final static String RULE_FILE_EXTENSION = ".rule";

    private final MainSettings mainSettings;

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
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try(Reader reader = new FileReader(filepath)) {
            RuleDefinition ruleDefinition = mapper.readValue(reader, RuleDefinition.class);
            ruleDefinition.validate();
            return ruleDefinition;
        }
    }

    public synchronized void saveRule(String oldName, RuleDefinition ruleDef) throws IOException {
        if (StringUtils.isBlank(ruleDef.getName())) return;

        String oldFilePath = oldName != null ? getFilePath(oldName, ruleDef) : null;
        String newFilePath = getFilePath(ruleDef.getName(), ruleDef);
        boolean isRenaming = oldFilePath != null && !oldFilePath.equals(newFilePath);

        if (oldName == null){
            // it is a new file
            if (new File(newFilePath).exists()){
                throw new FileAlreadyExistsException(newFilePath);
            }
        }
        else{
            if (isRenaming && new File(newFilePath).exists()){
                throw new FileAlreadyExistsException(newFilePath);
            }
        }

        new File(newFilePath).getParentFile().mkdirs();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ruleDef.clearErrors();
        mapper.writeValue(new FileWriter(newFilePath), ruleDef);

        if (isRenaming){
            // it is a rename, remove the old file
            boolean ignored = new File(oldFilePath).delete();
        }
    }

    private String encodeFile(String filename) {
        return filename.replaceAll("[/\\\\:.?*]", "_");
    }

    private String getFilePath(String name, RuleDefinition ruleDef){
        return mainSettings.getEzLoad().getRulesDir()+File.separator+ruleDef.getBroker().getDirName()+"_v"+ruleDef.getBrokerFileVersion()
                +File.separator+encodeFile(name)+RULE_FILE_EXTENSION;
    }

}
