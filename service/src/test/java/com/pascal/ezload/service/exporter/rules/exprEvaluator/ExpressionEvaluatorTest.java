package com.pascal.ezload.service.exporter.rules.exprEvaluator;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.util.LoggerReporting;
import com.pascal.ezload.service.util.ModelUtils;
import org.apache.commons.jexl3.JexlException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpressionEvaluatorTest {

    @Test
    public void testStringVariableConcatenation(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "debut");
        ezdata.put("ezload.two", "fin");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("debutfin", result);
    }



    @Test
    public void testStringVariableConcatWithInteger(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "debut");
        ezdata.put("ezload.two", "2500");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("debut2500", result);
    }


    @Test
    public void testStringVariableConcatWithFloat(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "debut");
        ezdata.put("ezload.two", ModelUtils.float2Str(2500.5f));
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("debut2500.5", result);
    }


    @Test
    public void testStringVariableNumberDot(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "debut");
        ezdata.put("ezload.two", "2500.50");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("debut2500.5", result);
    }


    @Test
    public void testStringVariableNumberComma(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "debut");
        ezdata.put("ezload.two", "2500,50");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("debut2500.5", result);
    }



    @Test
    public void testAdditionIntegers(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "2500");
        ezdata.put("ezload.two", "2500");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("5000", result);
    }


    @Test
    public void testAdditionFloatAndInteger(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", ModelUtils.float2Str(2500.5f));
        ezdata.put("ezload.two", "2500");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("5000,5", result);
    }

    @Test
    public void testAdditionFloats(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", ModelUtils.float2Str(2500.5f));
        ezdata.put("ezload.two", ModelUtils.float2Str(2500.5f));
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("5001", result);
    }


    @Test
    public void testAdditionIntegerAsString(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "+2 500.0");
        ezdata.put("ezload.two", "+2 500.0");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("5000", result);
    }


    @Test
    public void testAdditionFloatAsString(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "+2 500.5");
        ezdata.put("ezload.two", "+2 500.5");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("5001", result);
    }


    @Test
    public void testAdditionFloatAsString2(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "+2 500,5");
        ezdata.put("ezload.two", "+2 500,5");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("5001", result);
    }


    @Test
    public void testAdditionWithNegativeNumber(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "-2 500,5");
        ezdata.put("ezload.two", "+2 500,5");
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "ezload.one + ezload.two", ezdata);
        assertEquals("0", result);
    }


    @Test
    public void testExpression(){
        MainSettings mainSettings = new MainSettings();

        EzData ezdata = new EzData();
        ezdata.put("ezload.one", "20");
        assertThrows(ExpressionEvaluator.ExpressionException.class, () -> ExpressionEvaluator.getSingleton().evaluateAsString(new LoggerReporting(), mainSettings, "achat de ezload.one action", ezdata));
    }
}
