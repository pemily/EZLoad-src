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

    @Test
    public void testCleanFilename(){
        Assertions.assertEquals("ca-ltxt,;!avec #", StringUtils.cleanFileName("c:/\\a-l.txt,;!avec ~#*?"));
    }
}
