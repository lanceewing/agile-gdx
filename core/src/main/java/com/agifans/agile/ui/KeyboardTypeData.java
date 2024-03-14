package com.agifans.agile.ui;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Input.Keys;

/**
 * Data required by the KeyboardType enum.
 */
public class KeyboardTypeData {

    public static final int EXCLAIMATION_MARK = ((Keys.SHIFT_LEFT << 8) | Keys.NUM_1);
    public static final int DOUBLE_QUOTE = ((Keys.SHIFT_LEFT << 8) | Keys.NUM_2);
    public static final int HASH = Keys.UNKNOWN;   // TODO: No key constant for this one.
    public static final int DOLLAR_SIGN = ((Keys.SHIFT_LEFT << 8) | Keys.NUM_4);
    public static final int PERCENTAGE_SIGN = ((Keys.SHIFT_LEFT << 8) | Keys.NUM_5);
    public static final int AMPERSAND = ((Keys.SHIFT_LEFT << 8) | Keys.NUM_7);
    public static final int OPEN_BRACKET = ((Keys.SHIFT_LEFT << 8) | Keys.NUM_9);
    public static final int CLOSE_BRACKET = ((Keys.SHIFT_LEFT << 8) | Keys.NUM_0);
    public static final int ASTERISK = ((Keys.SHIFT_LEFT << 8) | Keys.NUM_8);
    public static final int PIPE = ((Keys.SHIFT_LEFT << 8) | Keys.BACKSLASH);
    public static final int COLON = ((Keys.SHIFT_LEFT << 8) | Keys.SEMICOLON);
    public static final int GREATER_THAN = ((Keys.SHIFT_LEFT << 8) | Keys.COMMA);
    public static final int LESS_THAN = ((Keys.SHIFT_LEFT << 8) | Keys.PERIOD);
    public static final int QUESTION_MARK = ((Keys.SHIFT_LEFT << 8) | Keys.SLASH);
    public static final int CARAT = ((Keys.SHIFT_LEFT << 8) | Keys.NUM_6);
    public static final int UNDERSCORE = ((Keys.SHIFT_LEFT << 8) | Keys.MINUS);
    public static final int OPEN_BRACE = ((Keys.SHIFT_LEFT << 8) | Keys.LEFT_BRACKET);
    public static final int CLOSE_BRACE = ((Keys.SHIFT_LEFT << 8) | Keys.RIGHT_BRACKET);
    public static final int TILDE = ((Keys.SHIFT_LEFT << 8) | Keys.UNKNOWN);
    public static final int AT = ((Keys.SHIFT_LEFT << 8) | Keys.APOSTROPHE);
    public static final int UPPER_A = ((Keys.SHIFT_LEFT << 8) | Keys.A);
    public static final int UPPER_B = ((Keys.SHIFT_LEFT << 8) | Keys.B);
    public static final int UPPER_C = ((Keys.SHIFT_LEFT << 8) | Keys.C);
    public static final int UPPER_D = ((Keys.SHIFT_LEFT << 8) | Keys.D);
    public static final int UPPER_E = ((Keys.SHIFT_LEFT << 8) | Keys.E);
    public static final int UPPER_F = ((Keys.SHIFT_LEFT << 8) | Keys.F);
    public static final int UPPER_G = ((Keys.SHIFT_LEFT << 8) | Keys.G);
    public static final int UPPER_H = ((Keys.SHIFT_LEFT << 8) | Keys.H);
    public static final int UPPER_I = ((Keys.SHIFT_LEFT << 8) | Keys.I);
    public static final int UPPER_J = ((Keys.SHIFT_LEFT << 8) | Keys.J);
    public static final int UPPER_K = ((Keys.SHIFT_LEFT << 8) | Keys.K);
    public static final int UPPER_L = ((Keys.SHIFT_LEFT << 8) | Keys.L);
    public static final int UPPER_M = ((Keys.SHIFT_LEFT << 8) | Keys.M);
    public static final int UPPER_N = ((Keys.SHIFT_LEFT << 8) | Keys.N);
    public static final int UPPER_O = ((Keys.SHIFT_LEFT << 8) | Keys.O);
    public static final int UPPER_P = ((Keys.SHIFT_LEFT << 8) | Keys.P);
    public static final int UPPER_Q = ((Keys.SHIFT_LEFT << 8) | Keys.Q);
    public static final int UPPER_R = ((Keys.SHIFT_LEFT << 8) | Keys.R);
    public static final int UPPER_S = ((Keys.SHIFT_LEFT << 8) | Keys.S);
    public static final int UPPER_T = ((Keys.SHIFT_LEFT << 8) | Keys.T);
    public static final int UPPER_U = ((Keys.SHIFT_LEFT << 8) | Keys.U);
    public static final int UPPER_V = ((Keys.SHIFT_LEFT << 8) | Keys.V);
    public static final int UPPER_W = ((Keys.SHIFT_LEFT << 8) | Keys.W);
    public static final int UPPER_X = ((Keys.SHIFT_LEFT << 8) | Keys.X);
    public static final int UPPER_Y = ((Keys.SHIFT_LEFT << 8) | Keys.Y);
    public static final int UPPER_Z = ((Keys.SHIFT_LEFT << 8) | Keys.Z);
    
    /**
     * These are the virtual keystrokes that we will also map to keyTyped calls, in addition
     * to keyDown and keyUp.
     */
    public static final Map<Integer, Character> KEYTYPED_CHAR_MAP = new HashMap<>();
    static {
        // Note: ESC doesn't go through libgdx keyTyped, so we don't map it here.
        
        KEYTYPED_CHAR_MAP.put(Keys.BACKSPACE, (char)8);
        KEYTYPED_CHAR_MAP.put(Keys.TAB, (char)9);
        KEYTYPED_CHAR_MAP.put(Keys.SPACE, (char)32);
        KEYTYPED_CHAR_MAP.put(EXCLAIMATION_MARK, (char)33);
        KEYTYPED_CHAR_MAP.put(DOUBLE_QUOTE, (char)34);
        KEYTYPED_CHAR_MAP.put(HASH, (char)35);
        KEYTYPED_CHAR_MAP.put(DOLLAR_SIGN, (char)36);
        KEYTYPED_CHAR_MAP.put(PERCENTAGE_SIGN, (char)37);
        KEYTYPED_CHAR_MAP.put(AMPERSAND, (char)38);
        KEYTYPED_CHAR_MAP.put(Keys.APOSTROPHE, (char)39);
        KEYTYPED_CHAR_MAP.put(OPEN_BRACKET, (char)40);
        
        KEYTYPED_CHAR_MAP.put(CLOSE_BRACKET, (char)41);
        KEYTYPED_CHAR_MAP.put(ASTERISK, (char)42);
        KEYTYPED_CHAR_MAP.put(Keys.PLUS, (char)43);
        KEYTYPED_CHAR_MAP.put(Keys.COMMA, (char)44);
        KEYTYPED_CHAR_MAP.put(Keys.MINUS, (char)45);
        KEYTYPED_CHAR_MAP.put(Keys.PERIOD, (char)46);
        KEYTYPED_CHAR_MAP.put(Keys.SLASH, (char)47);
        KEYTYPED_CHAR_MAP.put(Keys.NUM_0, (char)48);
        KEYTYPED_CHAR_MAP.put(Keys.NUM_1, (char)49);
        KEYTYPED_CHAR_MAP.put(Keys.NUM_2, (char)50);
        
        KEYTYPED_CHAR_MAP.put(Keys.NUM_3, (char)51);
        KEYTYPED_CHAR_MAP.put(Keys.NUM_4, (char)52);
        KEYTYPED_CHAR_MAP.put(Keys.NUM_5, (char)53);
        KEYTYPED_CHAR_MAP.put(Keys.NUM_6, (char)54);
        KEYTYPED_CHAR_MAP.put(Keys.NUM_7, (char)55);
        KEYTYPED_CHAR_MAP.put(Keys.NUM_8, (char)56);
        KEYTYPED_CHAR_MAP.put(Keys.NUM_9, (char)57);
        KEYTYPED_CHAR_MAP.put(COLON, (char)58);
        KEYTYPED_CHAR_MAP.put(Keys.SEMICOLON, (char)59);
        KEYTYPED_CHAR_MAP.put(LESS_THAN, (char)60);
        
        KEYTYPED_CHAR_MAP.put(Keys.EQUALS, (char)61);
        KEYTYPED_CHAR_MAP.put(GREATER_THAN, (char)62);
        KEYTYPED_CHAR_MAP.put(QUESTION_MARK, (char)63);
        KEYTYPED_CHAR_MAP.put(AT, (char)64);
        KEYTYPED_CHAR_MAP.put(UPPER_A, (char)65);
        KEYTYPED_CHAR_MAP.put(UPPER_B, (char)66);
        KEYTYPED_CHAR_MAP.put(UPPER_C, (char)67);
        KEYTYPED_CHAR_MAP.put(UPPER_D, (char)68);
        KEYTYPED_CHAR_MAP.put(UPPER_E, (char)69);
        KEYTYPED_CHAR_MAP.put(UPPER_F, (char)70);
        
        KEYTYPED_CHAR_MAP.put(UPPER_G, (char)71);
        KEYTYPED_CHAR_MAP.put(UPPER_H, (char)72);
        KEYTYPED_CHAR_MAP.put(UPPER_I, (char)73);
        KEYTYPED_CHAR_MAP.put(UPPER_J, (char)74);
        KEYTYPED_CHAR_MAP.put(UPPER_K, (char)75);
        KEYTYPED_CHAR_MAP.put(UPPER_L, (char)76);
        KEYTYPED_CHAR_MAP.put(UPPER_M, (char)77);
        KEYTYPED_CHAR_MAP.put(UPPER_N, (char)78);
        KEYTYPED_CHAR_MAP.put(UPPER_O, (char)79);
        KEYTYPED_CHAR_MAP.put(UPPER_P, (char)80);
        
        KEYTYPED_CHAR_MAP.put(UPPER_Q, (char)81);
        KEYTYPED_CHAR_MAP.put(UPPER_R, (char)82);
        KEYTYPED_CHAR_MAP.put(UPPER_S, (char)83);
        KEYTYPED_CHAR_MAP.put(UPPER_T, (char)84);
        KEYTYPED_CHAR_MAP.put(UPPER_U, (char)85);
        KEYTYPED_CHAR_MAP.put(UPPER_V, (char)86);
        KEYTYPED_CHAR_MAP.put(UPPER_W, (char)87);
        KEYTYPED_CHAR_MAP.put(UPPER_X, (char)88);
        KEYTYPED_CHAR_MAP.put(UPPER_Y, (char)89);
        KEYTYPED_CHAR_MAP.put(UPPER_Z, (char)90);
        
        KEYTYPED_CHAR_MAP.put(Keys.LEFT_BRACKET, (char)91);
        KEYTYPED_CHAR_MAP.put(Keys.BACKSLASH, (char)92);
        KEYTYPED_CHAR_MAP.put(Keys.RIGHT_BRACKET, (char)93);
        KEYTYPED_CHAR_MAP.put(CARAT, (char)94);
        KEYTYPED_CHAR_MAP.put(UNDERSCORE, (char)95);
        KEYTYPED_CHAR_MAP.put(Keys.GRAVE, (char)96);
        KEYTYPED_CHAR_MAP.put(Keys.A, (char)97);
        KEYTYPED_CHAR_MAP.put(Keys.B, (char)98);
        KEYTYPED_CHAR_MAP.put(Keys.C, (char)99);
        KEYTYPED_CHAR_MAP.put(Keys.D, (char)100);
        
        KEYTYPED_CHAR_MAP.put(Keys.E, (char)101);
        KEYTYPED_CHAR_MAP.put(Keys.F, (char)102);
        KEYTYPED_CHAR_MAP.put(Keys.G, (char)103);
        KEYTYPED_CHAR_MAP.put(Keys.H, (char)104);
        KEYTYPED_CHAR_MAP.put(Keys.I, (char)105);
        KEYTYPED_CHAR_MAP.put(Keys.J, (char)106);
        KEYTYPED_CHAR_MAP.put(Keys.K, (char)107);
        KEYTYPED_CHAR_MAP.put(Keys.L, (char)108);
        KEYTYPED_CHAR_MAP.put(Keys.M, (char)109);
        KEYTYPED_CHAR_MAP.put(Keys.N, (char)110);
        
        KEYTYPED_CHAR_MAP.put(Keys.O, (char)111);
        KEYTYPED_CHAR_MAP.put(Keys.P, (char)112);
        KEYTYPED_CHAR_MAP.put(Keys.Q, (char)113);
        KEYTYPED_CHAR_MAP.put(Keys.R, (char)114);
        KEYTYPED_CHAR_MAP.put(Keys.S, (char)115);
        KEYTYPED_CHAR_MAP.put(Keys.T, (char)116);
        KEYTYPED_CHAR_MAP.put(Keys.U, (char)117);
        KEYTYPED_CHAR_MAP.put(Keys.V, (char)118);
        KEYTYPED_CHAR_MAP.put(Keys.W, (char)119);
        KEYTYPED_CHAR_MAP.put(Keys.X, (char)120);
        
        KEYTYPED_CHAR_MAP.put(Keys.Y, (char)121);
        KEYTYPED_CHAR_MAP.put(Keys.Z, (char)122);
        KEYTYPED_CHAR_MAP.put(OPEN_BRACE, (char)123);
        KEYTYPED_CHAR_MAP.put(PIPE, (char)124);
        KEYTYPED_CHAR_MAP.put(CLOSE_BRACE, (char)125);
        KEYTYPED_CHAR_MAP.put(TILDE, (char)126);
        KEYTYPED_CHAR_MAP.put(Keys.FORWARD_DEL, (char)127);
    }
}
