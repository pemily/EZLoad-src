package com.pascal.bientotrentier.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BRDateTest {
    @Test
    public void testBeforeOrEquals(){
        assertTrue(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(2001, 10, 10)));
        assertTrue(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(2000, 10, 20)));
        assertTrue(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(2000, 5, 10)));
        assertTrue(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(2000, 5, 20)));
        assertFalse(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(2000, 5, 8)));
        assertFalse(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(1999, 5, 10)));
        assertFalse(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(1999, 4, 10)));
        assertFalse(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(1999, 6, 10)));
        assertFalse(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(1999, 5, 20)));
        assertFalse(new BRDate(2000, 5, 10).isBeforeOrEquals(new BRDate(1999, 5, 5)));
    }


    @Test
    public void isDateValid(){
        assertFalse(BRDate.parseFrenchDate("00/01/2021", '/').isValid());
        assertFalse(BRDate.parseFrenchDate("01/00/2021", '/').isValid());
        assertFalse(BRDate.parseFrenchDate("01/01/2000", '/').isValid());
        assertTrue(BRDate.parseFrenchDate("01/01/2021", '/').isValid());
    }
}
