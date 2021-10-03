package com.pascal.ezload.service.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
