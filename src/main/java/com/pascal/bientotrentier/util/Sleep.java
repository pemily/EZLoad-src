package com.pascal.bientotrentier.util;

import java.util.Date;

public class Sleep {

    public static void wait(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
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
