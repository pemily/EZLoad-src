package com.pascal.bientotrentier.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringUtilsTest {

    @Test
    public void testDivideByChars(){
        assertEquals("a firs", StringUtils.divide("a first test", 't')[0]);
        assertEquals(" test", StringUtils.divide("a first test", 't')[1]);
        assertEquals("a first", StringUtils.divide("a first\n t\rest", '\n', '\r')[0]);
        assertEquals(" t\rest", StringUtils.divide("a first\n t\rest", '\n', '\r')[1]);
        assertEquals("", StringUtils.divide("a first test", 'a')[0]);
        assertEquals(" first test", StringUtils.divide("a first test", 'a')[1]);
        assertEquals("a first test", StringUtils.divide("a first testz", 'z')[0]);
        assertEquals("", StringUtils.divide("a first testz", 'z')[1]);
        assertNull(StringUtils.divide("a first test", 'x'));
        assertNull(StringUtils.divide("", 'x'));
    }

    @Test
    public void testDivideByString(){
        assertEquals("a ", StringUtils.divide("a first test", "first")[0]);
        assertEquals(" test", StringUtils.divide("a first test", "first")[1]);
        assertEquals("", StringUtils.divide("a first test", "a first")[0]);
        assertEquals(" test", StringUtils.divide("a first test", "a first")[1]);
        assertEquals("a ", StringUtils.divide("a first test", "first test")[0]);
        assertEquals("", StringUtils.divide("a first test", "first test")[1]);
        assertEquals("", StringUtils.divide("a first test", "a first test")[0]);
        assertEquals("", StringUtils.divide("a first test", "a first test")[1]);
        assertNull(StringUtils.divide("a first test", " pas trouvé"));
    }

}
