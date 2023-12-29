package com.agifans.agile.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;

/**
 * GWT doesn't support String.format, so doing things like padding a String value 
 * needs to be done at a lower level.
 */
public class StringUtils {

    public static String padLeftZeros(String inputString, int length) {
        return padLeft(inputString, length, '0');
    }

    public static String padLeftSpaces(String inputString, int length) {
        return padLeft(inputString, length, ' ');
    }

    public static String padRightZeros(String inputString, int length) {
        return padRight(inputString, length, '0');
    }

    public static String padRightSpaces(String inputString, int length) {
        return padRight(inputString, length, ' ');
    }
    
    public static String padLeft(String inputString, int length, char padChar) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append(padChar);
        }
        sb.append(inputString);
        return sb.toString();
    }

    public static String padRight(String inputString, int length, char padChar) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder(inputString);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }
    
    public static String format(final String format, final Object... args) {
        StringBuilder sb = new StringBuilder();
        int cur = 0;
        int len = format.length();
        while (cur < len) {
            int fi = format.indexOf('{', cur);
            if (fi != -1) {
                sb.append(format.substring(cur, fi));
                int si = format.indexOf('}', fi);
                if (si != -1) {
                    String nStr = format.substring(fi + 1, si);
                    int i = Integer.parseInt(nStr);
                    sb.append(args[i]);
                    cur = si + 1;
                } else {
                    sb.append(format.substring(fi));
                    break;
                }
            } else {
                sb.append(format.substring(cur, len));
                break;
            }
        }
        return sb.toString();
    }
    
    public static byte[] getBytesFromString(String text) {
        byte[] textBytes = null;
        
        try {
            // GWT backend doesn't support IBM437/CP437.
            if (Gdx.app.getType() == ApplicationType.WebGL) {
                textBytes = text.getBytes(StandardCharsets.ISO_8859_1);
            }
            else {
                textBytes = text.getBytes("Cp437");
            }
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen.
            textBytes = text.getBytes(StandardCharsets.ISO_8859_1);
        }
        
        return textBytes;
    }
}
