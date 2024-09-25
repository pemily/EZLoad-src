/**
 * ezServer - EZLoad an automatic loader for EZPortfolio
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
package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.EZLoad;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.rules.CommonFunctions;
import com.pascal.ezload.service.exporter.rules.RuleDefinition;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.common.util.TextReporting;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Path("rule")
public class RulesHandler {

    @Inject
    private ProcessManager processManager;

    @Inject
    private EzServerState serverState;


    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAllRules() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        return new RulesManager(settingsManager, settingsManager.loadProps())
                .getAllRules()
                .stream()
                .map(RuleDefinition::getName)
                .collect(Collectors.toList());

    }

    @GET
    @Path("{broker}/{brokerFileVersion}")
    @Produces(MediaType.APPLICATION_JSON)
    public RuleDefinition getRule(@NotNull @PathParam("broker") EnumEZBroker broker,
                                  @NotNull @PathParam("brokerFileVersion") int brokerFileVersion,
                                  @NotNull @QueryParam("ruleName") String ruleName) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        return new RulesManager(settingsManager, settingsManager.loadProps())
                .getAllRules()
                .stream()
                .filter(ruleDef -> ruleDef.getName().equals(ruleName)
                                    && ruleDef.getBrokerFileVersion() == brokerFileVersion
                                    && ruleDef.getBroker() == broker)
                .findFirst()
                .orElse(null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RuleDefinition saveRule(@QueryParam("oldName") String oldName,
                         @NotNull RuleDefinition ruleDefinition) throws Exception {
        ruleDefinition.setEzLoadVersion(EZLoad.VERSION);
        SettingsManager settingsManager = SettingsManager.getInstance();
        String errorForName = new RulesManager(settingsManager, settingsManager.loadProps())
                .saveRule(oldName == null || StringUtils.isBlank(oldName) ? null : oldName, ruleDefinition);

        RuleDefinition result;
        if (errorForName == null) {
            result = getRule(ruleDefinition.getBroker(), ruleDefinition.getBrokerFileVersion(), ruleDefinition.getName());
        }
        else{
            ruleDefinition.validate();
            ruleDefinition.getField2ErrorMsg().put(RuleDefinition.Field.name.name(), errorForName);
            return ruleDefinition;
        }
        return result;
    }

    @DELETE
    @Path("rule")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteRule(@NotNull RuleDefinition ruleDefinition) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        new RulesManager(settingsManager, settingsManager.loadProps())
                        .delete(ruleDefinition);
    }

    @GET
    @Path("com/pascal/ezload/{broker}/{brokerFileVersion}")
    @Produces(MediaType.APPLICATION_JSON)
    public CommonFunctions getCommonFunction(@NotNull @PathParam("broker") EnumEZBroker broker,
                                             @NotNull @PathParam("brokerFileVersion") int brokerFileVersion) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        return new RulesManager(settingsManager, settingsManager.loadProps())
                .getCommonScript(broker, brokerFileVersion);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/saveCommon")
    // return the execution report, to check the syntax
    public String saveCommonFunction(@NotNull CommonFunctions commonFunction) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        new RulesManager(settingsManager, settingsManager.loadProps())
                .saveCommonScript(commonFunction);
        return validateCommonFunction(commonFunction);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/validate")
    // return the execution report, to check the syntax
    public String validateCommonFunction(@NotNull CommonFunctions commonFunction) {
        TextReporting reporting = new TextReporting();
        try {
            return ExpressionEvaluator.getSingleton().evaluateAsString(reporting, String.join("\n", commonFunction.getScript()), new EzData());
        }
        catch(Exception e){
            String errorMsg = e.getMessage();
            int index = errorMsg.lastIndexOf("=>");
            if (index == -1) return errorMsg;
            return errorMsg.substring(index+"=>".length());
        }
    }

}
