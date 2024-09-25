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

import com.pascal.ezload.common.util.finance.BourseDirectTools;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.common.util.HttpUtilCached;
import com.pascal.ezload.common.util.LoggerReporting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BourseDirectToolsTest {

    private HttpUtilCached cache() throws IOException {
        String dir = System.getProperty("java.io.tmpdir")+ File.separator+this.getClass().getSimpleName()+"_"+Math.random();
        new File(dir).mkdirs();
        return new HttpUtilCached(dir);
    }

    @Test
    public void testSearchBourseDirect_US88160R1014() throws IOException {
        EzData data = new EzData();
        data.put(new EzDataKey("ezOperation_Lieu"), "NASDAQ/NGS (GLOBAL SELECT MARKET)");
        Optional<EZShare> action = BourseDirectTools.searchAction(new LoggerReporting(), cache(),"US88160R1014", EnumEZBroker.BourseDirect, data);
        assertTrue(action.isPresent());
        Assertions.assertEquals("TSLA:NASDAQ", action.get().getGoogleCode());
    }

    @Test
    public void testSearchBourseDirect_US92936U1097() throws IOException {
        EzData data = new EzData();
        Optional<EZShare> action = BourseDirectTools.searchAction(new LoggerReporting(), cache(), "US92936U1097", EnumEZBroker.BourseDirect, data);
        assertTrue(action.isPresent());
        Assertions.assertEquals("WPC:NYSE", action.get().getGoogleCode());
    }


    @Test
    public void testSearchBourseDirect_FR0000063737() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "AUBAY");
        Optional<EZShare> action = BourseDirectTools.searchAction(new LoggerReporting(), cache(), "FR0000063737", EnumEZBroker.BourseDirect, ezData);
        assertEquals("AUB:EPA", action.get().getGoogleCode());
    }


    @Test
    public void testSearchBourseDirect_FR0000120578() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "SANOFI");
        Optional<EZShare> action = BourseDirectTools.searchAction(new LoggerReporting(), cache(), "FR0000120578", EnumEZBroker.BourseDirect, ezData);
        assertEquals("SAN:EPA", action.get().getGoogleCode());
    }

    @Test
    public void testSearchBourseDirect_DE000A1EWWW0() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "ADIDAS NOM.");
        Optional<EZShare> action = BourseDirectTools.searchAction(new LoggerReporting(), cache(), "DE000A1EWWW0", EnumEZBroker.BourseDirect, ezData);
        assertEquals("ADS:ETR", action.get().getGoogleCode());
    }

    @Test
    public void testSearchBourseDirect_FR0013269123() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "RUBIS");
        ezData.put(new EzDataKey("ezOperation_Lieu"), "BORSE BERLIN EQUIDUCT TRADING - BERL");
        Optional<EZShare> action = BourseDirectTools.searchAction(new LoggerReporting(), cache(), "FR0013269123", EnumEZBroker.BourseDirect, ezData);
        assertEquals("RUI:EPA", action.get().getGoogleCode());
    }

    @Test
    public void testSearchBourseDirect_FR0000120222() throws IOException {
        EzData ezData = new EzData();
        ezData.put(new EzDataKey("ezOperation_INFO3"), "RETRAIT OBLIG.");
        // j'ai eu ce cas lors d'un retrait d'obligation (espece sur OST dans INFO1) aucune autre info dans le pdf
        // mais le meme jour dans une autre opération, il y avait: INDEMNISATION FR0000120222 CNP ASSURANCES
        Optional<EZShare> action = BourseDirectTools.searchAction(new LoggerReporting(), cache(), "FR0000120222", EnumEZBroker.BourseDirect, ezData);
        ///////////////////////////////////////////// ICI Ce n'est pas le code que j'attendais, j'aurais voulu voir: https://www.google.com/finance/quote/CNPAF:OTCMKTS
        assertEquals("FR0000120222_EUR:LON", action.get().getGoogleCode());
    }


    @Test
    public void testSearchBourseDirect_FR0011871128() throws IOException {
        EzData ezData = new EzData();
        Optional<EZShare> action = BourseDirectTools.searchAction(new LoggerReporting(), cache(),  "FR0011871128", EnumEZBroker.BourseDirect, ezData);
        assertEquals("PSP5:EPA", action.get().getGoogleCode());
    }



}
