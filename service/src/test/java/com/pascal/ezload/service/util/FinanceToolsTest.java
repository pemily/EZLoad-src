package com.pascal.ezload.service.util;

import com.pascal.ezload.service.model.EZAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class FinanceToolsTest {

    @Test
    public void testSearchBourseDirect() throws IOException {
        EZAction action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "US92936U1097");
        assertNotNull(action);
        Assertions.assertEquals("US92936U1097", action.getIsin());
        Assertions.assertEquals("XNYS", action.getMarketPlace().getMic());
        Assertions.assertEquals("WP CAREY INC", action.getName());
        Assertions.assertEquals("WPC", action.getTicker());
    }


    @Test
    public void testSearchYahoo() throws IOException {
        EZAction action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "US92936U1097");
        assertNotNull(action);
        assertNull(action.getMarketPlace()); // don't know how to find it
        Assertions.assertEquals("W. P. Carey Inc.", action.getName());
        Assertions.assertEquals("WPC", action.getTicker());
    }

    @Test
    public void testSearchMarketStack() throws IOException {
        EZAction action = FinanceTools.getInstance().searchActionFromMarketstack(new LoggerReporting(), "WPC");
        assertNotNull(action);
        Assertions.assertEquals("XNYS", action.getMarketPlace().getMic());
        Assertions.assertEquals("W. P. Carey Inc", action.getName());
        Assertions.assertEquals("WPC", action.getTicker());
    }

}
