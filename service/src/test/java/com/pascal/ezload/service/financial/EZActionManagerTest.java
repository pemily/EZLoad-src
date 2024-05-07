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
package com.pascal.ezload.service.financial;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.EZActionManagerUtil;
import com.pascal.ezload.service.util.LoggerReporting;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class EZActionManagerTest {

    @Test
    public void testTeslaNoError() throws IOException {
        EZActionManager actionManager = EZActionManagerUtil.getEzActionManager();
        EzData data = new EzData();
        data.put(new EzDataKey("ezOperation_Lieu"), "NASDAQ/NGS (GLOBAL SELECT MARKET)");

        EZShare action = actionManager.getOrCreate(new LoggerReporting(), "US88160R1014", EnumEZBroker.BourseDirect, data);
        Assert.assertNotNull(action.getYahooCode());
        Assert.assertFalse(action.getYahooCode().isEmpty());
        Assert.assertNotNull(action.getSeekingAlphaCode());
        Assert.assertFalse(action.getSeekingAlphaCode().isEmpty());


        Assert.assertEquals(0, actionManager.computeActionErrors(new LoggerReporting(), action));

        // set a wrong yahoo code for tesla
        action.setYahooCode("RUI.PA");
        Assert.assertEquals(1, actionManager.computeActionErrors(new LoggerReporting(), action));

    }


}
