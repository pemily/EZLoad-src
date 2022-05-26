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
        Assertions.assertEquals("-2400", ModelUtils.float2Str(-2400f));
        Assertions.assertEquals("0", ModelUtils.float2Str(0f));
        Assertions.assertEquals("2400", ModelUtils.float2Str(2400.00f));
        Assertions.assertEquals("2400,5", ModelUtils.float2Str(2400.50f));
        Assertions.assertEquals("-2400,5", ModelUtils.float2Str(-2400.50000000001f));
        Assertions.assertEquals("-2400,5542", ModelUtils.float2Str(-2400.55426001f));
        Assertions.assertEquals("-2400,5", ModelUtils.float2Str(-2400.499999999999f));
    }

    @Test
    public void double2Str(){
        Assertions.assertEquals("0", ModelUtils.double2Str(0d));
        Assertions.assertEquals("2400", ModelUtils.double2Str(2400.00d));
        Assertions.assertEquals("2400,5", ModelUtils.double2Str(2400.50d));
        Assertions.assertEquals("-2400", ModelUtils.double2Str(-2400d));
        Assertions.assertEquals("-2400,5", ModelUtils.double2Str(-2400.50000000001d));
        Assertions.assertEquals("-2400,55426", ModelUtils.double2Str(-2400.55426001d));
        Assertions.assertEquals("-2400,499999", ModelUtils.double2Str(-2400.499999999999d));
        Assertions.assertEquals("-2400,49999", ModelUtils.double2Str(-2400.49999d));
        Assertions.assertEquals("-2400,499999", ModelUtils.double2Str(-2400.499999d));
        Assertions.assertEquals("-2400,499999", ModelUtils.double2Str(-2400.4999999d));
    }
}
