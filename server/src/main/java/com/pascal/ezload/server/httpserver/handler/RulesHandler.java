package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.EZLoad;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.rules.RuleDefinition;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.model.EnumEZBroker;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

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
    @Path("{ruleName}")
    @Produces(MediaType.APPLICATION_JSON)
    public RuleDefinition getRule(@NotNull EnumEZBroker broker,
                                  @NotNull int brokerFileVersion,
                                  @NotNull @PathParam("ruleName") String ruleName) throws Exception {
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
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveRule(@PathParam("oldName") String oldName,
                         @NotNull RuleDefinition ruleDefinition) throws Exception {
        ruleDefinition.setEzLoadVersion(EZLoad.VERSION);
        new RulesManager(SettingsManager.getInstance().loadProps())
                .saveRule(oldName, ruleDefinition);
    }
}
