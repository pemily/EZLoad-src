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
package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.rules.update.FileState;
import com.pascal.ezload.service.rules.update.RulesVersionManager;
import com.pascal.ezload.service.util.FileProcessor;
import com.pascal.ezload.service.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RulesManager {

    private final static String RULE_FILE_EXTENSION = ".rule";
    private final static String COMMON_FUNCTIONS_FILENAME = "common.script";

    private final RulesVersionManager rulesVersionManager;
    private final MainSettings mainSettings;
    private final SettingsManager settingsManager;
    private final Map<String, CommonFunctions> brokerAndFileVersion2CommonFunctionsCache = new HashMap<>();

    public RulesManager(SettingsManager settingsManager, MainSettings mainSettings) {
        this.mainSettings = mainSettings;
        this.settingsManager = settingsManager;
        this.rulesVersionManager = new RulesVersionManager(settingsManager.getEzLoadRepoDir(), mainSettings);
    }

    public synchronized List<RuleDefinition> getAllRules() throws IOException {
        return new FileProcessor(settingsManager.getDir(mainSettings.getEzLoad().getRulesDir()), d -> true, f -> f.getName().endsWith(RULE_FILE_EXTENSION))
                .mapFile(f -> {
                    try {
                        FileState state = rulesVersionManager.getState(f);
                        return readRule(f, state == FileState.NEW,
                                    state != FileState.NEW && state != FileState.NO_CHANGE);
                    } catch (IOException | GitAPIException e) {
                        throw new RuntimeException("Error while reading file: "+f, e);
                    }
                });
    }


    public synchronized RuleDefinition readRule(String filepath, boolean newUserRule, boolean dirtyFile) throws IOException {
        try(Reader reader = new FileReader(filepath, StandardCharsets.UTF_8)) {
            RuleDefinition ruleDefinition = JsonUtil.createDefaultMapper().readValue(reader, RuleDefinition.class);
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
        boolean isNewUserRule = ruleDef.isNewUserRule();
        ruleDef.beforeSave(RulesManager::normalize); // the before save set to null the newUserRule

        JsonUtil.createDefaultWriter().writeValue(new FileWriter(newFilePath, StandardCharsets.UTF_8), ruleDef);

        if (isRenaming && isNewUserRule){
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
        return settingsManager.getDir(mainSettings.getEzLoad().getRulesDir())+File.separator+broker.getDirName()+"_v"+brokerFileVersion;
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
                        return readCommonScript(broker, brokerFileVersion, rulesVersionManager.getState(getCommonFilePath(broker, brokerFileVersion)) != FileState.NO_CHANGE);
                    } catch (IOException | GitAPIException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private synchronized CommonFunctions readCommonScript(EnumEZBroker broker, int brokerFileVersion, boolean dirtyFile) throws IOException {
        String commonFile = getCommonFilePath(broker, brokerFileVersion);
        try (Reader reader = new FileReader(commonFile)) {
            CommonFunctions content = JsonUtil.createDefaultMapper().readValue(reader, CommonFunctions.class);
            content.setDirtyFile(dirtyFile);
            return content;
        }
    }

    public synchronized void saveCommonScript(CommonFunctions function) throws IOException {
        String commonFile = getCommonFilePath(function.getBroker(), function.getBrokerFileVersion());

        // to ease the comparison in github
        function.setScript(Arrays.stream(function.getScript()).map(RulesManager::normalizeNoTrim).collect(Collectors.toList()).toArray(new String[]{}));

        function.beforeSave();
        JsonUtil.createDefaultWriter().writeValue(new FileWriter(commonFile, StandardCharsets.UTF_8), function);

        brokerAndFileVersion2CommonFunctionsCache.put(function.getBroker().getDirName()+function.getBrokerFileVersion(), function);
    }

    public static String normalize(String line) {
        if (line == null) return null;
        line = line.replace("\t", "    "); // replace tabulation by 4 spaces
        line = line.trim();
        return line;
    }

    public static String normalizeNoTrim(String line) {
        if (line == null) return null;
        line = line.replace("\t", "    "); // replace tabulation by 4 spaces
        // no trim to not remove the indentation of function in common script
        return line;
    }

}
