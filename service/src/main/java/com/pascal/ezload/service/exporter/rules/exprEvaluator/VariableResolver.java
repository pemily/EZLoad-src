package com.pascal.ezload.service.exporter.rules.exprEvaluator;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.jexl3.JexlContext;


public class VariableResolver  implements JexlContext {

    public static final String VARIABLE_SYSTEM_NAME = "ez";

    private final EzData allVariables;
    private final MainSettings mainSettings;
    private final Reporting reporting;

    public VariableResolver(EzData allVariables, Reporting reporting, MainSettings mainSettings){
        this.allVariables = allVariables;
        this.mainSettings = mainSettings;
        this.reporting = reporting;
    }

    @Override
    public Object get(String name) {
        if (name.equals(VARIABLE_SYSTEM_NAME)){
            return new SystemFunction();
        }
        // search if the variable is a in the allVariables map
        return allVariables.get(name);
    }

    @Override
    public void set(String name, Object value) {
        throw new RuntimeException("Vous ne devez pas changer la valeur d'une variable");
    }

    @Override
    public boolean has(String name) {
        return VARIABLE_SYSTEM_NAME.equals(name) || allVariables.containsKey(name);
    }
}