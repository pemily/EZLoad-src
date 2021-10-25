package com.pascal.ezload.service.exporter.rules.exprEvaluator;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;

import java.util.Map;

//    https://us-east-2.console.aws.amazon.com/codesuite/codecommit/repositories/JarvisHome/browse/refs/heads/master/--/Nestor%40Home/nestorhome/src/main/java/com/emily/nestor/home/common/engine/util/exprEvaluator/VariableResolver.java?region=us-east-2

// http://commons.apache.org/proper/commons-jexl/reference/examples.html
//http://svn.apache.org/viewvc/commons/proper/jexl/trunk/src/test/java/org/apache/commons/jexl3/examples/MethodPropertyTest.java?view=markup
public class ExpressionEvaluator {

    private  final JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(false).create();

    private static final ExpressionEvaluator singleton = newInstance();

    public static ExpressionEvaluator getSingleton(){ return singleton; }

    public static ExpressionEvaluator newInstance(){
        return new ExpressionEvaluator();
    }

    private ExpressionEvaluator(){ }

    public Object evaluateAsObj(Reporting reporting, MainSettings mainSettings, String lineToEval, EzData allVariables) {
        Object result = null;
        try {
            // try to create an expression with the line to eval
            JexlExpression expression = jexl.createExpression(lineToEval);
            JexlContext jexlContext = new VariableResolver(allVariables, reporting, mainSettings);
            result = expression.evaluate(jexlContext);
        }
        catch(org.apache.commons.jexl3.JexlException.Tokenization e1){
            // oups, it was not an expression
            throw new ExpressionException("Evaluate: "+lineToEval+ " => tokenization problem: "+ e1.getMessage(), e1);
        }
        catch(org.apache.commons.jexl3.JexlException.Parsing e2){
            // oups, it was not an expression => use directly the lineToEval
            throw new ExpressionException("Evaluate: "+lineToEval+ " => parsing problem: "+ e2.getMessage(), e2);
        }
        catch(org.apache.commons.jexl3.JexlException.Variable e3){
            // the line contains a variable that does not exists (or it is not a variable)
            throw new ExpressionException("Evaluate: "+lineToEval+ " => not a variable: "+ e3.getMessage(), e3);
        }
        catch(Exception e){
            throw new ExpressionException("Evaluate: "+lineToEval+ " => problem: "+ e.getMessage(), e);
        }
        reporting.info("L'evaluation de: "+lineToEval+ " donne: "+ result);
        return result;
    }

    public boolean evaluateAsBoolean(Reporting reporting, MainSettings mainSettings, String lineToEval, EzData allVariables) {
        Object resultObj = evaluateAsObj(reporting, mainSettings, lineToEval, allVariables);

        if (resultObj != null && !(resultObj instanceof Boolean)) {
            throw new IllegalStateException("La condition: "+lineToEval+" ne retourne pas un boolean, elle retourne un type: "+resultObj.getClass().getSimpleName()+" dont la valeur est => "+resultObj);
        }
        if (resultObj == null){
            throw new IllegalStateException("La condition: "+lineToEval+" retourne null!!");
        }
        boolean result = (Boolean) resultObj;

        return result;
    }


    public String evaluateAsString(Reporting reporting, MainSettings mainSettings, String lineToEval, EzData allVariables) {
        Object resultObj = evaluateAsObj(reporting, mainSettings, lineToEval, allVariables);

        if (resultObj != null && !(resultObj instanceof String)) {
            throw new IllegalStateException("La condition: "+lineToEval+" ne retourne pas une String, elle retourne un type: "+resultObj.getClass().getSimpleName()+" dont la valeur est => "+resultObj);
        }
        if (resultObj == null){
            throw new IllegalStateException("La condition: "+lineToEval+" retourne null!!");
        }
        return (String) resultObj;
    }

    static class ExpressionException extends RuntimeException {
        ExpressionException(String msg, Exception e){
            super(msg, e);
        }
    }
}