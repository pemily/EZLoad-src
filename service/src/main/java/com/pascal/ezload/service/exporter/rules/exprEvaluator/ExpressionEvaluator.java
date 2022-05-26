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
import org.apache.commons.jexl3.*;

import java.util.Map;

//    https://us-east-2.console.aws.amazon.com/codesuite/codecommit/repositories/JarvisHome/browse/refs/heads/master/--/Nestor%40Home/nestorhome/src/main/java/com/emily/nestor/home/common/engine/util/exprEvaluator/VariableResolver.java?region=us-east-2

// http://commons.apache.org/proper/commons-jexl/reference/examples.html
//http://svn.apache.org/viewvc/commons/proper/jexl/trunk/src/test/java/org/apache/commons/jexl3/examples/MethodPropertyTest.java?view=markup
public class ExpressionEvaluator {

    private  final JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(false).create();

    private static final ExpressionEvaluator singleton = newInstance();

    public static ExpressionEvaluator getSingleton(){ return singleton; }

    private static ExpressionEvaluator newInstance(){
        return new ExpressionEvaluator();
    }

    private ExpressionEvaluator(){ }

    public Object evaluateAsObj(Reporting reporting, String lineToEval, EzData allVariables) {
        Object result;
        try {
            // try to create an expression with the line to eval
            JexlScript script = jexl.createScript(lineToEval);
            JexlContext jexlContext = new VariableResolver(allVariables);
            result = script.execute(jexlContext);
        }
        catch(org.apache.commons.jexl3.JexlException.Tokenization e1){
            // oups, it was not an expression
            throw new ExpressionException(" => Problème de tokenization: "+ e1.getMessage(), e1);
        }
        catch(org.apache.commons.jexl3.JexlException.Parsing e2){
            // oups, it was not an expression => use directly the lineToEval
            throw new ExpressionException(" => Problème de parsing: "+ e2.getMessage(), e2);
        }
        catch(org.apache.commons.jexl3.JexlException.Variable e3){
            // the line contains a variable that does not exists (or it is not a variable)
            throw new ExpressionException(" => Variable inconnue: "+ e3.getMessage(), e3);
        }
        catch(Exception e){
            throw new ExpressionException(" => Probleme inconnu: "+ e.getMessage(), e);
        }
        return result;
    }

    public boolean evaluateAsBoolean(Reporting reporting, String lineToEval, EzData allVariables) {
        Object resultObj = evaluateAsObj(reporting, lineToEval, allVariables);

        if (resultObj != null && !(resultObj instanceof Boolean)) {
            throw new IllegalStateException("La condition ne retourne pas un boolean, elle retourne un type: "+resultObj.getClass().getSimpleName()+" dont la valeur est => "+resultObj);
        }
        if (resultObj == null){
            throw new IllegalStateException("La condition retourne null!!");
        }
        boolean result = (Boolean) resultObj;

        return result;
    }


    public String evaluateAsString(Reporting reporting, String lineToEval, EzData allVariables) {
        Object resultObj = evaluateAsObj(reporting, lineToEval, allVariables);

        if (resultObj == null){
            throw new IllegalStateException("L'expression retourne null!!");
        }
        if (resultObj instanceof Float){
            // simplify if possible, in case the result of the expression is a float: 5000.00
            // convert it into an int if possible to obtain a string: 5000
            return ModelUtils.float2Str((Float)resultObj );
        }
        if (resultObj instanceof Double){
            // simplify if possible, in case the result of the expression is a float: 5000.00
            // convert it into an int if possible to obtain a string: 5000
            return ModelUtils.double2Str((Double)resultObj );
        }
        return resultObj+"";
    }

    static class ExpressionException extends RuntimeException {
        ExpressionException(String msg, Exception e){
            super(msg, e);
        }
    }
}