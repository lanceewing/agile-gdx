package com.agifans.agile;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Input.Keys;

/**
 * The AGI interpreter uses standard ASCII keycodes. This class is used to map
 * the libgdx keystrokes to standard ASCII and then to provide constants for use
 * within the AGILE interpreter. This includes CTRL key combinations that would 
 * result in an ASCI character, e.g. CTRL-I being the same as TAB.
 * 
 * Many of the libgdx characters result in the keyTyped of the InputAdapter being
 * invoked, but there are some that do not, such as the CTRL key combinations, but
 * also ESC. We let keyTyped handle the ones that it can, but for those that it 
 * can't, we provide the mapping so that keyDown can enqueue it instead.
 */
public class Character {
    
    /**
     * The CTRL modifier key.
     */
    private static final int CONTROL_MODIFIER = 0x20000;

    // ASCII characters
    public static final int CTRL_A = 1;
    public static final int CTRL_B = 2;
    public static final int CTRL_C = 3;
    public static final int CTRL_D = 4;
    public static final int CTRL_E = 5;
    public static final int CTRL_F = 6;
    public static final int CTRL_G = 7;
    public static final int CTRL_H = 8;
    public static final int BACKSPACE = 8;
    public static final int CTRL_I = 9;
    public static final int TAB = 9;
    public static final int CTRL_J = 10;
    public static final int CTRL_ENTER = 10;
    public static final int CTRL_K = 11;
    public static final int CTRL_L = 12;
    public static final int CTRL_M = 13;
    public static final int ENTER = 13;
    public static final int CTRL_N = 14;
    public static final int CTRL_O = 15;
    public static final int CTRL_P = 16;
    public static final int CTRL_Q = 17;
    public static final int CTRL_R = 18;
    public static final int CTRL_S = 19;
    public static final int CTRL_T = 20;
    public static final int CTRL_U = 21;
    public static final int CTRL_V = 22;
    public static final int CTRL_W = 23;
    public static final int CTRL_X = 24;
    public static final int CTRL_Y = 25;
    public static final int CTRL_Z = 26;

    public static final int ESC = 27;
    
    public static final int CTRL_BACK_SLASH = 28;
    public static final int CTRL_CLOSE_SQUARE_BRACKET = 29;
    public static final int CTRL_6 = 30;
    public static final int CTRL_MINUS = 31;
    
    public static final int SPACE = 32;
    public static final int EXCLAIMATION_MARK = 33;
    public static final int DOUBLE_QUOTE = 34;
    public static final int HASH = 35;
    public static final int DOLLAR_SIGN = 36;
    public static final int PERCENTAGE_SIGN = 37;
    public static final int AMPERSAND = 38;
    public static final int APOSTROPHE = 39;
    public static final int OPEN_BACKET = 40;
    public static final int CLOSE_BRACKET = 41;
    public static final int ASTERISK = 42;
    public static final int PLUS_SIGN = 43;
    public static final int COMMA = 44;
    public static final int MINUS_SIGN = 45;
    public static final int PERIOD = 46;
    public static final int FORWARD_SLASH = 47;
    
    public static final int NUM_0 = 48;
    public static final int NUM_1 = 49;
    public static final int NUM_2 = 50;
    public static final int NUM_3 = 51;
    public static final int NUM_4 = 52;
    public static final int NUM_5 = 53;
    public static final int NUM_6 = 54;
    public static final int NUM_7 = 55;
    public static final int NUM_8 = 56;
    public static final int NUM_9 = 57;
    
    public static final int COLON = 58;
    public static final int SEMI_COLON = 59;
    public static final int LESS_THAN = 60;
    public static final int EQUALS = 61;
    public static final int GREATER_THAN = 62;
    public static final int QUESTION_MARK = 63;
    public static final int AT_SIGN = 64;
    
    public static final int UPPER_A = 65;
    public static final int UPPER_B = 66;
    public static final int UPPER_C = 67;
    public static final int UPPER_D = 68;
    public static final int UPPER_E = 69;
    public static final int UPPER_F = 70;
    public static final int UPPER_G = 71;
    public static final int UPPER_H = 72;
    public static final int UPPER_I = 73;
    public static final int UPPER_J = 74;
    public static final int UPPER_K = 75;
    public static final int UPPER_L = 76;
    public static final int UPPER_M = 77;
    public static final int UPPER_N = 78;
    public static final int UPPER_O = 79;
    public static final int UPPER_P = 80;
    public static final int UPPER_Q = 81;
    public static final int UPPER_R = 82;
    public static final int UPPER_S = 83;
    public static final int UPPER_T = 84;
    public static final int UPPER_U = 85;
    public static final int UPPER_V = 86;
    public static final int UPPER_W = 87;
    public static final int UPPER_X = 88;
    public static final int UPPER_Y = 89;
    public static final int UPPER_Z = 90;
    
    public static final int OPEN_SQUARE_BRACKET = 91;
    public static final int BACK_SLASH = 92;
    public static final int CLOSE_SQUARE_BRACKET = 93;
    public static final int CARAT = 94;
    public static final int UNDERSCORE = 95;
    public static final int BACK_TICK = 96;
    
    public static final int LOWER_A = 97;
    public static final int LOWER_B = 98;
    public static final int LOWER_C = 99;
    public static final int LOWER_D = 100;
    public static final int LOWER_E = 101;
    public static final int LOWER_F = 102;
    public static final int LOWER_G = 103;
    public static final int LOWER_H = 104;
    public static final int LOWER_I = 105;
    public static final int LOWER_J = 106;
    public static final int LOWER_K = 107;
    public static final int LOWER_L = 108;
    public static final int LOWER_M = 109;
    public static final int LOWER_N = 110;
    public static final int LOWER_O = 111;
    public static final int LOWER_P = 112;
    public static final int LOWER_Q = 113;
    public static final int LOWER_R = 114;
    public static final int LOWER_S = 115;
    public static final int LOWER_T = 116;
    public static final int LOWER_U = 117;
    public static final int LOWER_V = 118;
    public static final int LOWER_W = 119;
    public static final int LOWER_X = 120;
    public static final int LOWER_Y = 121;
    public static final int LOWER_Z = 122;
    
    public static final int OPEN_BRACE = 123;
    public static final int PIPE = 124;
    public static final int CLOSE_BRACE = 125;
    public static final int TILDA = 126;

    
    public static final Map<Integer, Integer> KEYSTROKE_TO_CHAR_MAP = new HashMap<>();
    static {
        
        // LibGDX does not translate CTRL combinations into "keyTyped" calls.
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.A, CTRL_A);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.B, CTRL_B);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.C, CTRL_C);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.D, CTRL_D);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.E, CTRL_E);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.F, CTRL_F);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.G, CTRL_G);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.H, CTRL_H);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.I, CTRL_I);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.J, CTRL_J);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.ENTER, CTRL_ENTER);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.K, CTRL_K);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.L, CTRL_L);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.M, CTRL_M);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.N, CTRL_N);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.O, CTRL_O);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.P, CTRL_P);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.Q, CTRL_Q);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.R, CTRL_R);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.S, CTRL_S);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.T, CTRL_T);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.U, CTRL_U);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.V, CTRL_V);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.W, CTRL_W);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.X, CTRL_X);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.Y, CTRL_Y);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.Z, CTRL_Z);
        
        // ENTER goes through to keyTyped as 0x0A, i.e. LF!! So we map this ourselves to CR.
        KEYSTROKE_TO_CHAR_MAP.put(Keys.ENTER, ENTER);
        
        // ESC does not pass through to keyTyped either.
        KEYSTROKE_TO_CHAR_MAP.put(Keys.ESCAPE, ESC);
        

        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.BACKSLASH, CTRL_BACK_SLASH);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.LEFT_BRACKET, CTRL_CLOSE_SQUARE_BRACKET);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.NUM_6, CTRL_6);
        KEYSTROKE_TO_CHAR_MAP.put(CONTROL_MODIFIER + Keys.MINUS, CTRL_MINUS);
    }
}
