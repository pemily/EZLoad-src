package com.pascal.ezload.service.exporter.rules.exprEvaluator;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.ModelUtils;
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
        String v = allVariables.get(name);
        if (v != null){
            try {
                int i = ModelUtils.str2Int(v);
                return i;
            }
            catch(NumberFormatException e){
                try {
                    float f = ModelUtils.str2Float(v);
                    return f;
                }
                catch(NumberFormatException e2){
                    return v;
                }
            }
        }
        return v;
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