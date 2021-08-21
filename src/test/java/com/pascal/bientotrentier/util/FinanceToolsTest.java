package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.model.BRAction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class FinanceToolsTest {

    @Test
    public void testSearchBourseDirect() throws IOException {
        BRAction action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "US92936U1097");
        assertNotNull(action);
        assertEquals("US92936U1097", action.getIsin());
        assertEquals("XNYS", action.getMarketPlace().getMic());
        assertEquals("WP CAREY INC", action.getName());
        assertEquals("WPC", action.getTicker());
    }


    @Test
    public void testSearchYahoo() throws IOException {
        BRAction action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "US92936U1097");
        assertNotNull(action);
        assertNull(action.getMarketPlace()); // don't know how to find it
        assertEquals("W. P. Carey Inc.", action.getName());
        assertEquals("WPC", action.getTicker());
    }

    @Test
    public void testSearchMarketStack() throws IOException {
        BRAction action = FinanceTools.getInstance().searchActionFromMarketstack(new LoggerReporting(), "WPC");
        assertNotNull(action);
        assertEquals("XNYS", action.getMarketPlace().getMic());
        assertEquals("W. P. Carey Inc", action.getName());
        assertEquals("WPC", action.getTicker());
    }

}
