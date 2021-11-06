package com.pascal.ezload.service.util;

import com.pascal.ezload.service.exporter.ezPortfolio.v5.PRU;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.EZAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

public class FinanceToolsTest {

    @Test
    public void testSearchBourseDirect() throws IOException {
        ShareUtil shareUtil = new ShareUtil(new PRU(new SheetValues("A1:A1", new LinkedList<>())), new HashSet<>());
        EZAction action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "US92936U1097", shareUtil);
        assertNotNull(action);
        Assertions.assertEquals("US92936U1097", action.getIsin());
        Assertions.assertEquals("XNYS", action.getMarketPlace().getMic());
        Assertions.assertEquals("WP CAREY INC", action.getRawName());
        Assertions.assertEquals("WPC", action.getTicker());
    }


    @Test
    public void testSearchYahoo() throws IOException {
        EZAction action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "US92936U1097");
        assertNotNull(action);
        assertNull(action.getMarketPlace()); // don't know how to find it
        Assertions.assertEquals("W. P. Carey Inc.", action.getRawName());
        Assertions.assertEquals("WPC", action.getTicker());
    }

    @Test
    public void testSearchMarketStack() throws IOException {
        EZAction action = FinanceTools.getInstance().searchActionFromMarketstack(new LoggerReporting(), "WPC");
        assertNotNull(action);
        Assertions.assertEquals("XNYS", action.getMarketPlace().getMic());
        Assertions.assertEquals("W. P. Carey Inc", action.getRawName());
        Assertions.assertEquals("WPC", action.getTicker());
    }

}
