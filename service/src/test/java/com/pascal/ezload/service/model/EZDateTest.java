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
package com.pascal.ezload.service.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class EZDateTest {
    @Test
    public void testBeforeOrEquals(){
        Assertions.assertTrue(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(2001, 10, 10)));
        Assertions.assertTrue(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(2000, 10, 20)));
        Assertions.assertTrue(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(2000, 5, 10)));
        Assertions.assertTrue(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(2000, 5, 20)));
        Assertions.assertFalse(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(2000, 5, 8)));
        Assertions.assertFalse(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(1999, 5, 10)));
        Assertions.assertFalse(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(1999, 4, 10)));
        Assertions.assertFalse(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(1999, 6, 10)));
        Assertions.assertFalse(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(1999, 5, 20)));
        Assertions.assertFalse(new EZDate(2000, 5, 10).isBeforeOrEquals(new EZDate(1999, 5, 5)));
    }


    @Test
    public void isDateValid(){
        Assertions.assertFalse(EZDate.parseFrenchDate("00/01/2021", '/').isValid());
        Assertions.assertFalse(EZDate.parseFrenchDate("01/00/2021", '/').isValid());
        Assertions.assertFalse(EZDate.parseFrenchDate("01/01/2000", '/').isValid());
        Assertions.assertTrue(EZDate.parseFrenchDate("01/01/2021", '/').isValid());
    }

    @Test
    public void epochSecondDate(){
        Assertions.assertEquals(946681200, new EZDate(2000,01,01).toEpochSecond());
        Assertions.assertEquals(1660687200, new EZDate(2022,8,17).toEpochSecond());

        Assertions.assertEquals("2000/01/01", new EZDate(946681200).toYYMMDD());
        Assertions.assertEquals("2022/08/17", new EZDate(1660687200).toYYMMDD());

    }
}
