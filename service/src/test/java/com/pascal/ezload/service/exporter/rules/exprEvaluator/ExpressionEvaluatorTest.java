package com.pascal.ezload.service.exporter.rules.exprEvaluator;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;
import com.pascal.ezload.service.util.LoggerReporting;
import com.pascal.ezload.service.util.ModelUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpressionEvaluatorTest {

    @Test
    public void testStringVariableConcatenation(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "debut");
        ezdata.put(new EzDataKey("varTwo"), "fin");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("debutfin", result);
    }



    @Test
    public void testStringVariableConcatWithInteger(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "debut");
        ezdata.put(new EzDataKey("varTwo"), "2500");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("debut2500", result);
    }


    @Test
    public void testStringVariableConcatWithFloat(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "debut");
        ezdata.put(new EzDataKey("varTwo"), ModelUtils.float2Str(2500.5f));
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("debut2500.5", result);
    }


    @Test
    public void testStringVariableNumberDot(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "debut");
        ezdata.put(new EzDataKey("varTwo"), "2500.50");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("debut2500.5", result);
    }


    @Test
    public void testStringVariableNumberComma(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "debut");
        ezdata.put(new EzDataKey("varTwo"), "2500,50");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("debut2500.5", result);
    }



    @Test
    public void testAdditionIntegers(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "2500");
        ezdata.put(new EzDataKey("varTwo"), "2500");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(),  "varOne + varTwo", ezdata);
        assertEquals("5000", result);
    }


    @Test
    public void testAdditionFloatAndInteger(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), ModelUtils.float2Str(2500.5f));
        ezdata.put(new EzDataKey("varTwo"), "2500");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("5000,5", result);
    }

    @Test
    public void testAdditionFloats(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), ModelUtils.float2Str(2500.5f));
        ezdata.put(new EzDataKey("varTwo"), ModelUtils.float2Str(2500.5f));
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("5001", result);
    }


    @Test
    public void testAdditionIntegerAsString(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "+2 500.0");
        ezdata.put(new EzDataKey("varTwo"), "+2 500.0");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(),  "varOne + varTwo", ezdata);
        assertEquals("5000", result);
    }


    @Test
    public void testAdditionFloatAsString(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "+2 500.5");
        ezdata.put(new EzDataKey("varTwo"), "+2 500.5");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("5001", result);
    }


    @Test
    public void testAdditionFloatAsString2(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "+2 500,5");
        ezdata.put(new EzDataKey("varTwo"), "+2 500,5");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("5001", result);
    }


    @Test
    public void testAdditionWithNegativeNumber(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "-2 500,5");
        ezdata.put(new EzDataKey("varTwo"), "+2 500,5");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "varOne + varTwo", ezdata);
        assertEquals("0", result);
    }


    @Test
    public void testExpression(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "20");
        assertThrows(ExpressionEvaluator.ExpressionException.class, () -> ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "achat de varOne action", ezdata));
    }

    @Test
    public void testExpression2(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "20,5");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "-varOne", ezdata);
        assertEquals("-20,5", result);
    }


    @Test
    public void testExpression3(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "2988,36");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), "-varOne", ezdata);
        assertEquals("-2988,36", result);
    }
}
