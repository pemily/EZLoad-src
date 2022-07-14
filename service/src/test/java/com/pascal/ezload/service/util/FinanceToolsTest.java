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

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;
import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EnumEZBroker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

public class FinanceToolsTest {


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


    @Test
    public void testSearchDividendeHistory() throws IOException {
        List<FinanceTools.Dividend> dividends = FinanceTools.getInstance().searchDividends("US",  "WSR");
        Assertions.assertTrue(dividends.size() > 12);
    }


    @Test
    public void testSearchUS88160R1014() throws IOException {
        EzData data = new EzData();
        data.put(new EzDataKey("ezOperation_Lieu"), "NASDAQ/NGS (GLOBAL SELECT MARKET)");
        EZAction action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "US88160R1014", EnumEZBroker.BourseDirect, data);
        assertNotNull(action);
        Assertions.assertEquals("NASDAQ:TSLA", action.getEzTicker());
    }


    @Test
    public void testFR0000063737() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "AUBAY");
        EZAction action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "FR0000063737", EnumEZBroker.BourseDirect, ezData);
        assertEquals("EPA:AUB", action.getEzTicker());
    }


    @Test
    public void testFR0000120578() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "SANOFI");
        EZAction action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "FR0000120578", EnumEZBroker.BourseDirect, ezData);
        assertEquals("EPA:SAN", action.getEzTicker());
    }

    @Test
    public void testDE000A1EWWW0() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "ADIDAS NOM.");
        EZAction action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "DE000A1EWWW0", EnumEZBroker.BourseDirect, ezData);
        assertEquals("ETR:ADS", action.getEzTicker());
    }


    @Test
    public void testFR0000120222() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "RETRAIT OBLIG.");
        // j'ai eu ce cas lors d'un retrait d'obligation (espece sur OST dans INFO1) aucune autre info dans le pdf
        // mais le meme jour dans une autre opération, il y avait: INDEMNISATION FR0000120222 CNP ASSURANCES
        EZAction action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "FR0000120222", EnumEZBroker.BourseDirect, ezData);
        ///////////////////////////////////////////// ICI Ce n'est pas le code que j'attendais, j'aurais voulu voir: https://www.google.com/finance/quote/CNPAF:OTCMKTS
        assertEquals("FRA:FXNP", action.getEzTicker());
    }


}
