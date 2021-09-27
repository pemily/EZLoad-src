package com.pascal.ezload.service.util;

import java.util.Date;

public class Sleep {

    public static void waitSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
        }
    }

    public static void waitMillisecs(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
        }
    }

    public static long time(){
        return new Date().getTime();
    }

    public static boolean isOver(long time, int seconds){
        long now = new Date().getTime();
        long diff  = now - time;
        return diff > seconds*1000L;
    }

    public static boolean isBelow(long time, int seconds) {
        long now = new Date().getTime();
        long diff  = now - time;
        return diff < seconds*1000L;
    }
}
