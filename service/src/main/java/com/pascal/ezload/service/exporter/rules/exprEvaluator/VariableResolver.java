package com.pascal.ezload.service.exporter.rules.exprEvaluator;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.ModelUtils;
import org.apache.commons.jexl3.JexlContext;

import java.util.HashMap;
import java.util.Map;


public class VariableResolver  implements JexlContext {

    public static final String VARIABLE_SYSTEM_NAME = "ez";

    private final EzData allVariables;

    private final Map<String, Object> scriptVariables = new HashMap<>();

    public VariableResolver(EzData allVariables){
        this.allVariables = allVariables;
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

        return scriptVariables.get(name);
    }

    @Override
    public void set(String name, Object value) {
        scriptVariables.put(name, value);
    }

    @Override
    public boolean has(String name) {
        return VARIABLE_SYSTEM_NAME.equals(name) || allVariables.containsKey(name);
    }
}