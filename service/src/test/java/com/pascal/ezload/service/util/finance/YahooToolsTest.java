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
package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.util.DeviseUtil;
import com.pascal.ezload.service.util.HttpUtilCached;
import com.pascal.ezload.service.util.LoggerReporting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class YahooToolsTest {

    private HttpUtilCached cache() throws IOException {
        String dir = System.getProperty("java.io.tmpdir")+ File.separator+this.getClass().getSimpleName()+"_"+Math.random();
        new File(dir).mkdirs();
        return new HttpUtilCached(dir);
    }


    @Test
    public void testSearchYahoo_US88160R1014() throws Exception {
        Optional<EZShare> action = YahooTools.searchAction(new LoggerReporting(), cache(),"US88160R1014");
        Assertions.assertEquals("Tesla, Inc.", action.get().getEzName());
        Assertions.assertNull(action.get().getGoogleCode());
        Assertions.assertEquals("TSLA", action.get().getYahooCode());
        Assertions.assertEquals("US", action.get().getCountryCode());
        Assertions.assertEquals("Auto Manufacturers", action.get().getIndustry());
        Assertions.assertEquals("Consumer Cyclical", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("US88160R1014", action.get().getIsin());
    }


    @Test
    public void testSearchYahoo_US92936U1097() throws Exception {
        Optional<EZShare> action = YahooTools.searchAction(new LoggerReporting(), cache(), "US92936U1097");
        Assertions.assertEquals("W. P. Carey Inc.", action.get().getEzName());
        Assertions.assertNull(action.get().getGoogleCode());
        Assertions.assertEquals("WPC", action.get().getYahooCode());
        Assertions.assertEquals("US", action.get().getCountryCode());
        Assertions.assertEquals("REIT—Diversified", action.get().getIndustry());
        Assertions.assertEquals("Real Estate", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("US92936U1097", action.get().getIsin());
    }


    @Test
    public void testSearchYahoo_FR0000063737() throws Exception {
        Optional<EZShare> action = YahooTools.searchAction(new LoggerReporting(), cache(), "FR0000063737");
        Assertions.assertEquals("Aubay Société Anonyme", action.get().getEzName());
        Assertions.assertNull(action.get().getGoogleCode());
        Assertions.assertEquals("AUB.PA", action.get().getYahooCode());
        Assertions.assertEquals("FR", action.get().getCountryCode());
        Assertions.assertEquals("Information Technology Services", action.get().getIndustry());
        Assertions.assertEquals("Technology", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("FR0000063737", action.get().getIsin());
    }

    @Test
    public void testSearchYahoo_FR0000120578() throws Exception {
        Optional<EZShare> action = YahooTools.searchAction(new LoggerReporting(), cache(), "FR0000120578");
        Assertions.assertEquals("Sanofi", action.get().getEzName());
        Assertions.assertNull(action.get().getGoogleCode());
        Assertions.assertEquals("SAN.PA", action.get().getYahooCode());
        Assertions.assertEquals("FR", action.get().getCountryCode());
        Assertions.assertEquals("Drug Manufacturers—General", action.get().getIndustry());
        Assertions.assertEquals("Healthcare", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("FR0000120578", action.get().getIsin());
    }

    @Test
    public void testSearchYahoo_DE000A1EWWW0() throws Exception {
        Optional<EZShare> action = YahooTools.searchAction(new LoggerReporting(), cache(), "DE000A1EWWW0");
        Assertions.assertEquals("adidas AG", action.get().getEzName());
        Assertions.assertNull(action.get().getGoogleCode());
        Assertions.assertEquals("ADS.DE", action.get().getYahooCode());
        Assertions.assertEquals("DE", action.get().getCountryCode());
        Assertions.assertEquals("Footwear & Accessories", action.get().getIndustry());
        Assertions.assertEquals("Consumer Cyclical", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("DE000A1EWWW0", action.get().getIsin());
    }

    @Test
    public void testSearchYahoo_FR0013269123() throws Exception {
        Optional<EZShare> action = YahooTools.searchAction(new LoggerReporting(), cache(), "FR0013269123");
        Assertions.assertEquals("Rubis", action.get().getEzName());
        Assertions.assertNull(action.get().getGoogleCode());
        Assertions.assertEquals("RUI.PA", action.get().getYahooCode());
        Assertions.assertEquals("FR", action.get().getCountryCode());
        Assertions.assertEquals("Oil & Gas Refining & Marketing", action.get().getIndustry());
        Assertions.assertEquals("Energy", action.get().getSector());
        Assertions.assertEquals("Equity", action.get().getType());
        Assertions.assertEquals("FR0013269123", action.get().getIsin());
    }


    @Test
    public void testSearchYahoo_FR0000120222() throws Exception {
        Optional<EZShare> action = YahooTools.searchAction(new LoggerReporting(), cache(), "FR0000120222");
        assertTrue(action.isEmpty());
    }

    @Test
    public void testSearchYahoo_FR0011871128() throws Exception {
        Optional<EZShare> action = YahooTools.searchAction(new LoggerReporting(), cache(), "FR0011871128");
        Assertions.assertEquals("Lyxor PEA S&P 500 UCITS ETF", action.get().getEzName());
        Assertions.assertNull(action.get().getGoogleCode());
        Assertions.assertEquals("PSP5.PA", action.get().getYahooCode());
        Assertions.assertEquals("FR", action.get().getCountryCode());
        Assertions.assertNull(action.get().getIndustry());
        Assertions.assertNull(action.get().getSector());
        Assertions.assertEquals("ETF", action.get().getType());
        Assertions.assertEquals("FR0011871128", action.get().getIsin());
    }


    @Test
    public void testCurrencyMap() throws Exception {
        EZDate from = EZDate.parseYYYMMDDDate("2022/09/04", '/');
        EZDate to = EZDate.parseYYYMMDDDate("2022/09/11", '/');
        List<EZDate> dates = List.of(from, to);
        CurrencyMap currencyMap = YahooTools.getCurrencyMap(new LoggerReporting(), cache(), DeviseUtil.USD, DeviseUtil.EUR, dates);
        float px = currencyMap.getTargetPrice(new PriceAtDate(from, 1));
        assertNotEquals(0, px);
        px = currencyMap.getTargetPrice(new PriceAtDate(to, 1));
        assertNotEquals(0, px);
    }

    @Test
    public void testCurrencyMap2() throws Exception {
        EZDate from = EZDate.parseYYYMMDDDate("2021/01/01", '/');
        EZDate to = EZDate.parseYYYMMDDDate("2023/01/22", '/');
        List<EZDate> dates = List.of(from, to);
        CurrencyMap currencyMap = YahooTools.getCurrencyMap(new LoggerReporting(), cache(), DeviseUtil.USD, DeviseUtil.EUR, dates);
        float px = currencyMap.getTargetPrice(new PriceAtDate(from, 1));
        assertNotEquals(0, px);
        px = currencyMap.getTargetPrice(new PriceAtDate(to, 1));
        assertNotEquals(0, px);
    }

    @Test
    public void testSearchDividends() throws Exception {
        Optional<EZShare> action = YahooTools.searchAction(new LoggerReporting(), cache(), "US92936U1097");
        Assertions.assertEquals("W. P. Carey Inc.", action.get().getEzName());
        List<Dividend> dividends = YahooTools.searchDividends(new LoggerReporting(), cache(), action.get(), EZDate.parseYYYMMDDDate("2021/01/01", '/'),
                EZDate.parseYYYMMDDDate("2021/12/31", '/'));
        Assertions.assertEquals(4, dividends.size());
        Assertions.assertEquals("1.048000", dividends.get(0).getAmount());
        Assertions.assertEquals("2021/03/30", dividends.get(0).getDate().toYYYYMMDD());
        Assertions.assertEquals("1.050000", dividends.get(1).getAmount());
        Assertions.assertEquals("2021/06/29", dividends.get(1).getDate().toYYYYMMDD());
        Assertions.assertEquals("1.052000", dividends.get(2).getAmount());
        Assertions.assertEquals("2021/09/29", dividends.get(2).getDate().toYYYYMMDD());
        Assertions.assertEquals("1.055000", dividends.get(3).getAmount());
        Assertions.assertEquals("2021/12/30", dividends.get(3).getDate().toYYYYMMDD());
    }

}
