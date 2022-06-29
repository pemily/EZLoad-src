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

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;
import com.pascal.ezload.service.util.LoggerReporting;
import com.pascal.ezload.service.util.ModelUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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


    @Test
    public void testVariableIsNotDefined(){
        EzData ezdata = new EzData();
        boolean result = ExpressionEvaluator.getSingleton().evaluateAsBoolean(new LoggerReporting(), "ez.isDefined('varOne') && varOne != 0", ezdata);
        assertFalse(result);
    }

    @Test
    public void testVariableIsDefined(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "0");
        boolean result = ExpressionEvaluator.getSingleton().evaluateAsBoolean(new LoggerReporting(), "ez.isDefined('varOne') && varOne != 0", ezdata);
        assertFalse(result);
    }

    @Test
    public void testVariableIsDefinedAndNotZero(){
        EzData ezdata = new EzData();
        ezdata.put(new EzDataKey("varOne"), "1000");
        boolean result = ExpressionEvaluator.getSingleton().evaluateAsBoolean(new LoggerReporting(), "ez.isDefined('varOne') && varOne != 0", ezdata);
        assertTrue(result);
    }
}
