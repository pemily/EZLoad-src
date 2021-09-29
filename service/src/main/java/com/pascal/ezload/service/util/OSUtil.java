package com.pascal.ezload.service.util;

public class OSUtil {
    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS, OTHER
    };

    private static OS os = null;

    public static OS getOS() {
        if (os == null) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("nix") || operSys.contains("nux")
                    || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac") || operSys.contains("darwin")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            } else if (operSys.contains("win")){
                os = OS.WINDOWS;
            } else{
                os = OS.OTHER;
            }
        }
        return os;
    }
}