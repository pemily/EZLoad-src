package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.EZLoad;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.rules.CommonFunctions;
import com.pascal.ezload.service.exporter.rules.RuleDefinition;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.LoggerReporting;
import com.pascal.ezload.service.util.TextReporting;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
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
        return new RulesManager(SettingsManager.getInstance().loadProps())
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
        return new RulesManager(SettingsManager.getInstance().loadProps())
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
        String errorForName = new RulesManager(SettingsManager.getInstance().loadProps())
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteRule(@NotNull RuleDefinition ruleDefinition) throws Exception {
        new RulesManager(SettingsManager.getInstance().loadProps())
                .delete(ruleDefinition);
    }


    @GET
    @Path("common/{broker}/{brokerFileVersion}")
    @Produces(MediaType.APPLICATION_JSON)
    public CommonFunctions getCommonFunction(@NotNull @PathParam("broker") EnumEZBroker broker,
                                             @NotNull @PathParam("brokerFileVersion") int brokerFileVersion) throws Exception {
        return new RulesManager(SettingsManager.getInstance().loadProps())
                .readCommonScript(broker, brokerFileVersion);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/saveCommon")
    // return the execution report, to check the syntax
    public String saveCommonFunction(@NotNull CommonFunctions commonFunction) throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        new RulesManager(mainSettings).saveCommonScript(commonFunction);
        return validateCommonFunction(commonFunction);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/validate")
    // return the execution report, to check the syntax
    public String validateCommonFunction(@NotNull CommonFunctions commonFunction) throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        TextReporting reporting = new TextReporting();
        try {
            return ExpressionEvaluator.getSingleton().evaluateAsString(reporting, Arrays.stream(commonFunction.getScript()).collect(Collectors.joining("\n")), new EzData());
        }
        catch(Exception e){
            String errorMsg = e.getMessage();
            int index = errorMsg.lastIndexOf("=>");
            if (index == -1) return errorMsg;
            return errorMsg.substring(index+"=>".length());
        }
    }

}
