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