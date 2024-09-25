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
package com.pascal.ezload.common.util;

import java.util.Base64;

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


    public static String cleanFileName(String text){
        // strips off all non-ASCII characters
        text = text.replaceAll("[^\\x00-\\x7F]", "");

        // erases all the ASCII control characters
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // removes non-printable characters from Unicode
        text = text.replaceAll("\\p{C}", "");

        text = text.replaceAll("[.:/\\\\~*?]", "");

        return text.trim();
    }


    public static byte[] hexStrToByteArray(String s){
        return Base64.getDecoder().decode(s);
    }

    public static String byteArrayToHexStr(byte[] b){
        return Base64.getEncoder().encodeToString(b);
    }

    public static boolean isBlank(String s){
        return org.apache.commons.lang3.StringUtils.isBlank(s);
    }
}
