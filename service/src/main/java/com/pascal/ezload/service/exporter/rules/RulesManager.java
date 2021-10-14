package com.pascal.ezload.service.exporter.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.sources.FileProcessor;
import com.pascal.ezload.service.sources.Reporting;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RulesManager {

    private final static String RULE_FILE_EXTENSION = ".rule";

    private final MainSettings mainSettings;
    private final Reporting reporting;

    public RulesManager(Reporting reporting, MainSettings mainSettings) {
        this.reporting = reporting;
        this.mainSettings = mainSettings;
    }

    public List<RuleDefinition> getAllRules() throws IOException {
        return new FileProcessor(mainSettings.getEZLoad().getRulesDir(), d -> false, f -> f.getName().endsWith(RULE_FILE_EXTENSION))
                .mapFile(f -> {
                    try {
                        return readRule(f);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

    public RuleDefinition readRule(String filepath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try(Reader reader = new FileReader(filepath)) {
            return mapper.readValue(reader, RuleDefinition.class);
        }
    }

    public void saveRule(RuleDefinition ruleDef) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new FileWriter(ruleDef.getCourtier().getDirName()+"-"+encodeFile(ruleDef.getName())+RULE_FILE_EXTENSION), ruleDef);
    }

    private String encodeFile(String filename) {
        return filename.replaceAll("[/\\\\:.?*]", "_");
    }

}
