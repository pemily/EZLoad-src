package com.pascal.ezload.service.util;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {

    @Test
    public void testDivideByChars(){
        Assertions.assertEquals("a firs", StringUtils.divide("a first test", 't')[0]);
        Assertions.assertEquals(" test", StringUtils.divide("a first test", 't')[1]);
        Assertions.assertEquals("a first", StringUtils.divide("a first\n t\rest", '\n', '\r')[0]);
        Assertions.assertEquals(" t\rest", StringUtils.divide("a first\n t\rest", '\n', '\r')[1]);
        Assertions.assertEquals("", StringUtils.divide("a first test", 'a')[0]);
        Assertions.assertEquals(" first test", StringUtils.divide("a first test", 'a')[1]);
        Assertions.assertEquals("a first test", StringUtils.divide("a first testz", 'z')[0]);
        Assertions.assertEquals("", StringUtils.divide("a first testz", 'z')[1]);
        assertNull(StringUtils.divide("a first test", 'x'));
        assertNull(StringUtils.divide("", 'x'));
        Assertions.assertEquals("Mon Compte ", StringUtils.divide("Mon Compte (PEA)", '(', ')')[0]);
        Assertions.assertEquals("PEA)", StringUtils.divide("Mon Compte (PEA)", '(', ')')[1]);
    }

    @Test
    public void testDivideByString(){
        Assertions.assertEquals("a ", StringUtils.divide("a first test", "first")[0]);
        Assertions.assertEquals(" test", StringUtils.divide("a first test", "first")[1]);
        Assertions.assertEquals("", StringUtils.divide("a first test", "a first")[0]);
        Assertions.assertEquals(" test", StringUtils.divide("a first test", "a first")[1]);
        Assertions.assertEquals("a ", StringUtils.divide("a first test", "first test")[0]);
        Assertions.assertEquals("", StringUtils.divide("a first test", "first test")[1]);
        Assertions.assertEquals("", StringUtils.divide("a first test", "a first test")[0]);
        Assertions.assertEquals("", StringUtils.divide("a first test", "a first test")[1]);
        Assertions.assertEquals("a", StringUtils.divide("a first test", " ")[0]);
        Assertions.assertEquals("first test", StringUtils.divide("a first test", " ")[1]);
        assertNull(StringUtils.divide("a first test", " pas trouvé"));
    }

}
