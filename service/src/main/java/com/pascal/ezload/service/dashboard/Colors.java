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
package com.pascal.ezload.service.dashboard;

import java.util.ArrayList;
import java.util.List;

public class Colors {

    private List<String> colors;
    private int colorIndex = 0;


    public Colors(int numColors){
        colors = generateRainbowPalette(numColors);
    }

    public ColorCode nextColorCode(){
        return new ColorCode(colors.get(colorIndex++));
    }

    private static List<String> generateRainbowPalette(int numColors){
        var toRet = new ArrayList<String>();
        var phase = 0;
        var center = 128;
        var width = 127;
        var frequency = Math.PI*2/numColors;
        for (var i = 0; i < numColors; ++i)
        {
            var red   = (int) (Math.sin(frequency*i+2+phase) * width + center);
            var green = (int) (Math.sin(frequency*i+0+phase) * width + center);
            var blue  = (int) (Math.sin(frequency*i+4+phase) * width + center);
            /*
            //generate hex string:
            var redHex = Integer.toHexString(red);
            var greenHex = Integer.toHexString(green);
            var blueHex = Integer.toHexString(blue);
            */
            toRet.add("rgba("+red+","+green+","+blue);
        }
        return toRet;
    }



    private static List<String> generateRainbowPalette2(int numColors)
    {
        var toRet = new ArrayList<String>();
        var n = (float)numColors;
        for(var i = 0; i< numColors; i++)
        {
            int red = 255;
            int green = 0;
            int blue = 0;
            //red: (first quarter)
            if (i <= n / 4)
            {
                red = 255;
                green = (int)(255 / (n / 4) * i);
                blue = 0;
            }
            else if (i <= n / 2)  //2nd quarter
            {
                red = (int)((-255)/(n/4)*i + 255 * 2);
                green = 255;
                blue = 0;
            }
            else if (i <= (.75)*n)
            { // 3rd quarter
                red = 0;
                green = 255;
                blue = (int)(255 / (n / 4) * i + (-255 * 2));
            }
            else if(i > (.75)*n)
            {
                red = 0;
                green = (int)(-255 * i / (n / 4) + (255 * 4));
                blue = 255;
            }
            /*
            //generate hex string:
            var redHex = Integer.toHexString(red);
            var greenHex = Integer.toHexString(green);
            var blueHex = Integer.toHexString(blue);
            */
            toRet.add("rgba("+red+","+green+","+blue+",1)");
        }
        return toRet;
    }


    public class ColorCode {

        private String code;

        private ColorCode(String code){
            this.code = code;
        }

        private String getCode(){
            return code;
        }

        public String getColor(float transparency){
            return getCode()+","+transparency+")";
        }
    }
}
