package com.pascal.ezload.service.dashboard;

import java.util.ArrayList;
import java.util.List;

public class Colors {

    private List<String> colors;
    private int colorIndex = 0;


    public Colors(int numColors){
        colors = generateRainbowPalette(numColors);
    }

    public String nextColor(){
        if (colorIndex >= colors.size()){
            colorIndex = 0;
        }
        return colors.get(colorIndex++);
    }

    private static List<String> generateRainbowPalette(int numColors)
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

}
