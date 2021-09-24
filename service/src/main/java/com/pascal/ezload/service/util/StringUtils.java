package com.pascal.ezload.service.util;

public class StringUtils {

    public static String clean(String text) {
        text = text.replace("\r\n", "\n"); // \r\n => \n
        text = text.replace("\n\r", "\n"); // \n\r => \n
        text = text.replace("\r", ""); // \r => ""
        return text;
    }

    public static String[] divide(String text, char... splitChars){
        int index = -1;
        for (int i = 0; i < text.length(); i++) {
            int c = text.charAt(i);
            for (char splitChar : splitChars) {
                if (c == splitChar) {
                    index = i;
                    break;
                }
            }
            if (index != -1) break;
        }
        if (index == -1) return null;
        return new String[]{ text.substring(0, index), text.substring(index+1)};
    }



    public static String[] divide(String textToDivide, String centerText){
        int index = textToDivide.indexOf(centerText);
        if (index == -1) return null;
        if (index+centerText.length() > textToDivide.length()) return null;
        return new String[]{ textToDivide.substring(0, index), textToDivide.substring(index+centerText.length())};
    }
}
