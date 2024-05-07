package com.pascal.ezload.service.util;

import com.pascal.ezload.service.financial.EZActionManager;

import java.io.File;
import java.io.IOException;

public class EZActionManagerUtil {


    public static EZActionManager getEzActionManager() throws IOException {
        String dir = System.getProperty("java.io.tmpdir")+ File.separator+EZActionManagerUtil.class.getSimpleName()+"_"+Math.random();
        new File(dir).mkdirs();
        EZActionManager actionManager = new EZActionManager(dir, dir+ File.separator+"shares.json");
        return actionManager;
    }

}
