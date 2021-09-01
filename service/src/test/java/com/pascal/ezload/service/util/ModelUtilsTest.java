package com.pascal.ezload.service.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ModelUtilsTest {


    @Test
    public void normalizeAmount(){
        Assertions.assertEquals("1235,01", ModelUtils.normalizeAmount("+1 235.01"));
        Assertions.assertEquals("-1235,01", ModelUtils.normalizeAmount("-1 235.01"));
        Assertions.assertEquals("-1235", ModelUtils.normalizeAmount("-1 235.00"));
        Assertions.assertEquals("1235", ModelUtils.normalizeAmount("1 235,00000"));
        Assertions.assertEquals("1235000", ModelUtils.normalizeAmount("1 235 000"));
    }

    @Test
    public void str2Float(){
        Assertions.assertEquals(0f, ModelUtils.str2Float(null));
        Assertions.assertEquals(2400f, ModelUtils.str2Float("2400,00"));
        Assertions.assertEquals(-2400f, ModelUtils.str2Float("-2400,00"));
    }


    @Test
    public void float2Str(){
        Assertions.assertEquals("0", ModelUtils.float2Str(0f));
        Assertions.assertEquals("2400", ModelUtils.float2Str(2400.00f));
        Assertions.assertEquals("2400,5", ModelUtils.float2Str(2400.50f));
        Assertions.assertEquals("-2400", ModelUtils.float2Str(-2400f));
    }
}
