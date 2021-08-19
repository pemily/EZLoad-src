package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.model.BRAction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class FinanceToolsTest {

    @Test
    public void test() throws IOException {
        BRAction action = FinanceTools.getInstance().get(new LoggerReporting(), "US92936U1097");
        assertNotNull(action);
        assertEquals("US", action.getCountry());
        assertEquals("USD", action.getCurrencyCode());
        assertEquals("$", action.getCurrencySymbol());
        assertEquals("US92936U1097", action.getIsin());
        assertEquals("XNYS", action.getMarketMic());
        assertEquals("NEW YORK STOCK EXCHANGE, INC.", action.getMarketName());
        assertEquals("WP CAREY INC", action.getName());
        assertEquals("WPC", action.getTicker());

    }
}
