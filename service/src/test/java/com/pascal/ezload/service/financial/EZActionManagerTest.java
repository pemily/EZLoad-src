package com.pascal.ezload.service.financial;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.LoggerReporting;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class EZActionManagerTest {

    @Test
    public void testTeslaNoError() throws IOException {
        String dir = System.getProperty("java.io.tmpdir")+File.separator+this.getClass().getSimpleName()+"_"+Math.random();
        new File(dir).mkdirs();
        EZActionManager actionManager = new EZActionManager(dir, dir+ File.separator+"shares.json");
        EzData data = new EzData();
        data.put(new EzDataKey("ezOperation_Lieu"), "NASDAQ/NGS (GLOBAL SELECT MARKET)");

        EZShare action = actionManager.getOrCreate(new LoggerReporting(), "US88160R1014", EnumEZBroker.BourseDirect, data);
        Assert.assertNotNull(action.getYahooCode());
        Assert.assertFalse(action.getYahooCode().isEmpty());
        Assert.assertNotNull(action.getSeekingAlphaCode());
        Assert.assertFalse(action.getSeekingAlphaCode().isEmpty());

        ActionWithMsg msg = actionManager.getActionWithError();
        Assert.assertEquals(0, msg.getErrors().size());

        // set a wrong yahoo code for tesla
        action.setYahooCode("RUI.PA");
        msg = actionManager.getActionWithError();
        Assert.assertEquals(1, msg.getErrors().size());

    }


}
