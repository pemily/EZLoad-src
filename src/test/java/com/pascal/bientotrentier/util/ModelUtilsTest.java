package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.model.BRDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelUtilsTest {


    @Test
    public void normalizeAmount(){
        assertEquals("1235,01", ModelUtils.normalizeAmount("+1 235.01"));
        assertEquals("-1235,01", ModelUtils.normalizeAmount("-1 235.01"));
        assertEquals("-1235", ModelUtils.normalizeAmount("-1 235.00"));
        assertEquals("1235", ModelUtils.normalizeAmount("1 235,00000"));
        assertEquals("1235000", ModelUtils.normalizeAmount("1 235 000"));
    }

    @Test
    public void str2Float(){
        assertEquals(0f, ModelUtils.str2Float(null));
        assertEquals(2400f, ModelUtils.str2Float("2400,00"));
        assertEquals(-2400f, ModelUtils.str2Float("-2400,00"));
    }


    @Test
    public void float2Str(){
        assertEquals("0", ModelUtils.float2Str(0f));
        assertEquals("2400", ModelUtils.float2Str(2400.00f));
        assertEquals("2400,5", ModelUtils.float2Str(2400.50f));
        assertEquals("-2400", ModelUtils.float2Str(-2400f));
    }
}
