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
import java.util.Optional;

public class FinanceToolsTest {


    @Test
    public void testSearchMarketStack() throws IOException {
        EZAction action = FinanceTools.getInstance().searchActionFromMarketstack(new LoggerReporting(), "WPC");
        assertNotNull(action);
        Assertions.assertEquals("WPC", action.getEzTicker());
        Assertions.assertEquals("W. P. Carey Inc", action.getEzName());
    }


    @Test
    public void testSearchDividendeHistory() throws IOException {
        List<FinanceTools.Dividend> dividends = FinanceTools.getInstance().searchDividends("US",  "WSR");
        Assertions.assertTrue(dividends.size() > 12);
    }


    @Test
    public void testSearchBourseDirect_US88160R1014() throws IOException {
        EzData data = new EzData();
        data.put(new EzDataKey("ezOperation_Lieu"), "NASDAQ/NGS (GLOBAL SELECT MARKET)");
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "US88160R1014", EnumEZBroker.BourseDirect, data);
        assertTrue(action.isPresent());
        Assertions.assertEquals("NASDAQ:TSLA", action.get().getEzTicker());
    }

    @Test
    public void testSearchBourseDirect_US92936U1097() throws IOException {
        EzData data = new EzData();
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "US92936U1097", EnumEZBroker.BourseDirect, data);
        assertTrue(action.isPresent());
        Assertions.assertEquals("NYSE:WPC", action.get().getEzTicker());
    }


    @Test
    public void testSearchBourseDirect_FR0000063737() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "AUBAY");
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "FR0000063737", EnumEZBroker.BourseDirect, ezData);
        assertEquals("EPA:AUB", action.get().getEzTicker());
    }


    @Test
    public void testSearchBourseDirect_FR0000120578() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "SANOFI");
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "FR0000120578", EnumEZBroker.BourseDirect, ezData);
        assertEquals("EPA:SAN", action.get().getEzTicker());
    }

    @Test
    public void testSearchBourseDirect_DE000A1EWWW0() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "ADIDAS NOM.");
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "DE000A1EWWW0", EnumEZBroker.BourseDirect, ezData);
        assertEquals("ETR:ADS", action.get().getEzTicker());
    }

    @Test
    public void testSearchBourseDirect_FR0013269123() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "RUBIS");
        ezData.put(new EzDataKey("ezOperation_Lieu"), "BORSE BERLIN EQUIDUCT TRADING - BERL");
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "FR0013269123", EnumEZBroker.BourseDirect, ezData);
        assertEquals("EPA:RUI", action.get().getEzTicker());
    }

    @Test
    public void testSearchBourseDirect_FR0000120222() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "RETRAIT OBLIG.");
        // j'ai eu ce cas lors d'un retrait d'obligation (espece sur OST dans INFO1) aucune autre info dans le pdf
        // mais le meme jour dans une autre opération, il y avait: INDEMNISATION FR0000120222 CNP ASSURANCES
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "FR0000120222", EnumEZBroker.BourseDirect, ezData);
        ///////////////////////////////////////////// ICI Ce n'est pas le code que j'attendais, j'aurais voulu voir: https://www.google.com/finance/quote/CNPAF:OTCMKTS
        assertEquals("LON:FR0000120222_EUR", action.get().getEzTicker());
    }


    @Test
    public void testSearchBourseDirect_FR0011871128() throws IOException {
        EzData ezData = new EzData();
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromBourseDirect(new LoggerReporting(), "FR0011871128", EnumEZBroker.BourseDirect, ezData);
        assertEquals("EPA:PSP5", action.get().getEzTicker());
    }


    @Test
    public void testSearchYahoo_US88160R1014() throws IOException {
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "US88160R1014");
        Assertions.assertEquals("Tesla, Inc.", action.get().getEzName());
        Assertions.assertNull(action.get().getEzTicker());
        Assertions.assertEquals("TSLA", action.get().getYahooSymbol());
        Assertions.assertEquals("US", action.get().getCountryCode());
        Assertions.assertEquals("Auto Manufacturers", action.get().getIndustry());
        Assertions.assertEquals("Consumer Cyclical", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("US88160R1014", action.get().getIsin());
    }


    @Test
    public void testSearchYahoo_US92936U1097() throws IOException {
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "US92936U1097");
        Assertions.assertEquals("W. P. Carey Inc.", action.get().getEzName());
        Assertions.assertNull(action.get().getEzTicker());
        Assertions.assertEquals("WPC", action.get().getYahooSymbol());
        Assertions.assertEquals("US", action.get().getCountryCode());
        Assertions.assertEquals("REIT—Diversified", action.get().getIndustry());
        Assertions.assertEquals("Real Estate", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("US92936U1097", action.get().getIsin());
    }


    @Test
    public void testSearchYahoo_FR0000063737() throws IOException {
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "FR0000063737");
        Assertions.assertEquals("Aubay Société Anonyme", action.get().getEzName());
        Assertions.assertNull(action.get().getEzTicker());
        Assertions.assertEquals("AUB.PA", action.get().getYahooSymbol());
        Assertions.assertEquals("FR", action.get().getCountryCode());
        Assertions.assertEquals("Information Technology Services", action.get().getIndustry());
        Assertions.assertEquals("Technology", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("FR0000063737", action.get().getIsin());
    }

    @Test
    public void testSearchYahoo_FR0000120578() throws IOException {
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "FR0000120578");
        Assertions.assertEquals("Sanofi", action.get().getEzName());
        Assertions.assertNull(action.get().getEzTicker());
        Assertions.assertEquals("SAN.PA", action.get().getYahooSymbol());
        Assertions.assertEquals("FR", action.get().getCountryCode());
        Assertions.assertEquals("Drug Manufacturers—General", action.get().getIndustry());
        Assertions.assertEquals("Healthcare", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("FR0000120578", action.get().getIsin());
    }

    @Test
    public void testSearchYahoo_DE000A1EWWW0() throws IOException {
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "DE000A1EWWW0");
        Assertions.assertEquals("adidas AG", action.get().getEzName());
        Assertions.assertNull(action.get().getEzTicker());
        Assertions.assertEquals("ADS.DE", action.get().getYahooSymbol());
        Assertions.assertEquals("DE", action.get().getCountryCode());
        Assertions.assertEquals("Footwear & Accessories", action.get().getIndustry());
        Assertions.assertEquals("Consumer Cyclical", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("DE000A1EWWW0", action.get().getIsin());
    }

    @Test
    public void testSearchYahoo_FR0013269123() throws IOException {
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "FR0013269123");
        Assertions.assertEquals("Rubis", action.get().getEzName());
        Assertions.assertNull(action.get().getEzTicker());
        Assertions.assertEquals("RUI.PA", action.get().getYahooSymbol());
        Assertions.assertEquals("FR", action.get().getCountryCode());
        Assertions.assertEquals("Oil & Gas Refining & Marketing", action.get().getIndustry());
        Assertions.assertEquals("Energy", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("FR0013269123", action.get().getIsin());
    }


    @Test
    public void testSearchYahoo_FR0000120222() throws IOException {
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "FR0000120222");
        assertTrue(action.isEmpty());
    }

    @Test
    public void testSearchYahoo_FR0011871128() throws IOException {
        Optional<EZAction> action = FinanceTools.getInstance().searchActionFromYahooFinance(new LoggerReporting(), "FR0011871128");
        Assertions.assertEquals("Lyxor PEA S&P 500 UCITS ETF", action.get().getEzName());
        Assertions.assertNull(action.get().getEzTicker());
        Assertions.assertEquals("PSP5.PA", action.get().getYahooSymbol());
        Assertions.assertEquals("FR", action.get().getCountryCode());
        Assertions.assertNull(action.get().getIndustry());
        Assertions.assertNull(action.get().getSector());
        Assertions.assertEquals("ETF", action.get().getType());
        Assertions.assertEquals("FR0011871128", action.get().getIsin());
    }

}
