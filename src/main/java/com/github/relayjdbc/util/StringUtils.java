package com.github.relayjdbc.util;

public class StringUtils {
    // Helper method (can't use the 1.4-Method because support for 1.3 is desired)
    public static String[] split(String url) {
        char[] splitChars = { ',', ';', '#', '$' };

        for(int i = 0; i < splitChars.length; i++) {
            int charindex = url.indexOf(splitChars[i]);

            if(charindex >= 0) {
                return new String[] { url.substring(0, charindex), url.substring(charindex + 1) };
            }
        }

        return new String[] { url, "" };
    }
}
