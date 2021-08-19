package com.pascal.bientotrentier.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelUtilsTest {

    @Test
    public void isDateValid(){
        assertFalse(ModelUtils.isValidDate(null));
        assertFalse(ModelUtils.isValidDate(""));
        assertFalse(ModelUtils.isValidDate("00/01/2021"));
        assertFalse(ModelUtils.isValidDate("01/00/2021"));
        assertFalse(ModelUtils.isValidDate("01/01/2000"));
        assertTrue(ModelUtils.isValidDate("01/01/2021"));
    }

    @Test
    public void normalizeAmount(){
        assertEquals("1235,01", ModelUtils.normalizeAmount("+1 235.01"));
        assertEquals("-1235,01", ModelUtils.normalizeAmount("-1 235.01"));
    }
}
