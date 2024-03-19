package com.agifans.agile.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;

import static com.agifans.agile.ui.KeyboardTypeData.*;

/**
 * Enum representing the different types of keyboard available within AGILE.
 * 
 * @author Lance Ewing
 */
public enum KeyboardType {

    LANDSCAPE_LOWER_CASE(
            new Integer[][] {
              { Keys.ESCAPE,         Keys.ESCAPE,         Keys.ESCAPE,         Keys.ESCAPE,         Keys.F1,           Keys.F1,           Keys.F1,           Keys.F1,           Keys.F2,           Keys.F2,           Keys.F2,       Keys.F2,       Keys.F3,       Keys.F3,       Keys.F3,    Keys.F3,    Keys.F4,    Keys.F4,    Keys.F4,    Keys.F4,    Keys.F5,    Keys.F5,    Keys.F5,    Keys.F5,    Keys.F6,    Keys.F6,    Keys.F6,    Keys.F6,    Keys.F7,    Keys.F7,    Keys.F7,    Keys.F7,    Keys.F8,    Keys.F8,        Keys.F8,        Keys.F8,          Keys.F9,          Keys.F9,          Keys.F9,          Keys.F9,          Keys.F10,         Keys.F10,  Keys.F10,   Keys.F10,   Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE },
              { Keys.TAB,            Keys.TAB,            Keys.TAB,            Keys.TAB,            Keys.TAB,          Keys.Q,            Keys.Q,            Keys.Q,            Keys.Q,            Keys.W,            Keys.W,        Keys.W,        Keys.W,        Keys.E,        Keys.E,     Keys.E,     Keys.E,     Keys.R,     Keys.R,     Keys.R,     Keys.R,     Keys.T,     Keys.T,     Keys.T,     Keys.T,     Keys.Y,     Keys.Y,     Keys.Y,     Keys.Y,     Keys.U,     Keys.U,     Keys.U,     Keys.U,     Keys.I,         Keys.I,         Keys.I,           Keys.I,           Keys.O,           Keys.O,           Keys.O,           Keys.O,           Keys.P,    Keys.P,     Keys.P,     Keys.P,         Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER },
              { Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,    Keys.CAPS_LOCK,    Keys.A,            Keys.A,            Keys.A,            Keys.A,            Keys.S,        Keys.S,        Keys.S,        Keys.S,        Keys.D,     Keys.D,     Keys.D,     Keys.D,     Keys.F,     Keys.F,     Keys.F,     Keys.F,     Keys.G,     Keys.G,     Keys.G,     Keys.G,     Keys.H,     Keys.H,     Keys.H,     Keys.H,     Keys.J,     Keys.J,     Keys.J,     Keys.J,         Keys.K,         Keys.K,           Keys.K,           Keys.K,           Keys.L,           Keys.L,           Keys.L,           Keys.L,    Keys.ENTER, Keys.ENTER, Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER },
              { Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,   Keys.SHIFT_LEFT,   Keys.SHIFT_LEFT,   Keys.Z,            Keys.Z,            Keys.Z,            Keys.Z,        Keys.X,        Keys.X,        Keys.X,        Keys.X,     Keys.C,     Keys.C,     Keys.C,     Keys.C,     Keys.V,     Keys.V,     Keys.V,     Keys.V,     Keys.B,     Keys.B,     Keys.B,     Keys.B,     Keys.N,     Keys.N,     Keys.N,     Keys.N,     Keys.M,     Keys.M,     Keys.M,         Keys.M,         Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.UP,   Keys.UP,    Keys.UP,    Keys.UP,        Keys.EQUALS,    Keys.EQUALS,    Keys.EQUALS,    Keys.EQUALS },
              { Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.ALT_RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT,   Keys.ALT_RIGHT,   Keys.LEFT,        Keys.LEFT,        Keys.LEFT,        Keys.LEFT,        Keys.DOWN, Keys.DOWN,  Keys.DOWN,  Keys.DOWN,      Keys.RIGHT,     Keys.RIGHT,     Keys.RIGHT,     Keys.RIGHT }
            },
            "png/landscape_keyboard_lowercase.png",
            0.4f,
            110,
            0,
            176,
            1568,
            0
          ),
    LANDSCAPE_UPPER_CASE(
            new Integer[][] {
              { Keys.ESCAPE,         Keys.ESCAPE,         Keys.ESCAPE,         Keys.ESCAPE,         Keys.F1,           Keys.F1,           Keys.F1,           Keys.F1,           Keys.F2,           Keys.F2,           Keys.F2,       Keys.F2,       Keys.F3,       Keys.F3,       Keys.F3,    Keys.F3,    Keys.F4,    Keys.F4,    Keys.F4,    Keys.F4,    Keys.F5,    Keys.F5,    Keys.F5,    Keys.F5,    Keys.F6,    Keys.F6,    Keys.F6,    Keys.F6,    Keys.F7,    Keys.F7,    Keys.F7,    Keys.F7,    Keys.F8,    Keys.F8,        Keys.F8,        Keys.F8,          Keys.F9,          Keys.F9,          Keys.F9,          Keys.F9,          Keys.F10,         Keys.F10,  Keys.F10,   Keys.F10,   Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE },
              { Keys.TAB,            Keys.TAB,            Keys.TAB,            Keys.TAB,            Keys.TAB,          UPPER_Q,           UPPER_Q,           UPPER_Q,           UPPER_Q,           UPPER_W,           UPPER_W,       UPPER_W,       UPPER_W,       UPPER_E,       UPPER_E,    UPPER_E,    UPPER_E,    UPPER_R,    UPPER_R,    UPPER_R,    UPPER_R,    UPPER_T,    UPPER_T,    UPPER_T,    UPPER_T,    UPPER_Y,    UPPER_Y,    UPPER_Y,    UPPER_Y,    UPPER_U,    UPPER_U,    UPPER_U,    UPPER_U,    UPPER_I,        UPPER_I,        UPPER_I,          UPPER_I,          UPPER_O,          UPPER_O,          UPPER_O,          UPPER_O,          UPPER_P,   UPPER_P,    UPPER_P,    UPPER_P,        Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER },
              { Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,    Keys.CAPS_LOCK,    UPPER_A,           UPPER_A,           UPPER_A,           UPPER_A,           UPPER_S,       UPPER_S,       UPPER_S,       UPPER_S,       UPPER_D,    UPPER_D,    UPPER_D,    UPPER_D,    UPPER_F,    UPPER_F,    UPPER_F,    UPPER_F,    UPPER_G,    UPPER_G,    UPPER_G,    UPPER_G,    UPPER_H,    UPPER_H,    UPPER_H,    UPPER_H,    UPPER_J,    UPPER_J,    UPPER_J,    UPPER_J,        UPPER_K,        UPPER_K,          UPPER_K,          UPPER_K,          UPPER_L,          UPPER_L,          UPPER_L,          UPPER_L,   Keys.ENTER, Keys.ENTER, Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER },
              { Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,   Keys.SHIFT_LEFT,   Keys.SHIFT_LEFT,   UPPER_Z,           UPPER_Z,           UPPER_Z,           UPPER_Z,       UPPER_X,       UPPER_X,       UPPER_X,       UPPER_X,    UPPER_C,    UPPER_C,    UPPER_C,    UPPER_C,    UPPER_V,    UPPER_V,    UPPER_V,    UPPER_V,    UPPER_B,    UPPER_B,    UPPER_B,    UPPER_B,    UPPER_N,    UPPER_N,    UPPER_N,    UPPER_N,    UPPER_M,    UPPER_M,    UPPER_M,        UPPER_M,        Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.UP,   Keys.UP,    Keys.UP,    Keys.UP,        Keys.EQUALS,    Keys.EQUALS,    Keys.EQUALS,    Keys.EQUALS },
              { Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.ALT_RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT,   Keys.ALT_RIGHT,   Keys.LEFT,        Keys.LEFT,        Keys.LEFT,        Keys.LEFT,        Keys.DOWN, Keys.DOWN,  Keys.DOWN,  Keys.DOWN,      Keys.RIGHT,     Keys.RIGHT,     Keys.RIGHT,     Keys.RIGHT }
            },
            "png/landscape_keyboard_uppercase.png",
            0.4f,
            110,
            0,
            176,
            1568,
            0
          ),
    LANDSCAPE_PUNC_NUMBERS(
            new Integer[][] {
              { Keys.GRAVE,          Keys.GRAVE,          Keys.GRAVE,          Keys.GRAVE,          Keys.NUM_1,        Keys.NUM_1,        Keys.NUM_1,        Keys.NUM_1,        Keys.NUM_2,        Keys.NUM_2,        Keys.NUM_2,        Keys.NUM_2,         Keys.NUM_3,         Keys.NUM_3,         Keys.NUM_3,         Keys.NUM_3,  Keys.NUM_4,  Keys.NUM_4,  Keys.NUM_4,  Keys.NUM_4,  Keys.NUM_5,  Keys.NUM_5,      Keys.NUM_5,      Keys.NUM_5,      Keys.NUM_6,      Keys.NUM_6, Keys.NUM_6,     Keys.NUM_6,     Keys.NUM_7,     Keys.NUM_7,      Keys.NUM_7,      Keys.NUM_7,      Keys.NUM_8,      Keys.NUM_8,     Keys.NUM_8,     Keys.NUM_8,       Keys.NUM_9,       Keys.NUM_9,       Keys.NUM_9,       Keys.NUM_9,       Keys.NUM_0,       Keys.NUM_0,     Keys.NUM_0,     Keys.NUM_0,     Keys.BACKSPACE, Keys.BACKSPACE,  Keys.BACKSPACE,   Keys.BACKSPACE,   Keys.BACKSPACE },
              { Keys.PLUS,           Keys.PLUS,           Keys.PLUS,           Keys.PLUS,           Keys.PLUS,         EXCLAIMATION_MARK, EXCLAIMATION_MARK, EXCLAIMATION_MARK, EXCLAIMATION_MARK, DOUBLE_QUOTE,      DOUBLE_QUOTE,      DOUBLE_QUOTE,       DOUBLE_QUOTE,       HASH,               HASH,               HASH,        HASH,        DOLLAR_SIGN, DOLLAR_SIGN, DOLLAR_SIGN, DOLLAR_SIGN, PERCENTAGE_SIGN, PERCENTAGE_SIGN, PERCENTAGE_SIGN, PERCENTAGE_SIGN, AMPERSAND,  AMPERSAND,      AMPERSAND,      AMPERSAND,      Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.APOSTROPHE, OPEN_BRACKET,   OPEN_BRACKET,   OPEN_BRACKET,     OPEN_BRACKET,     CLOSE_BRACKET,    CLOSE_BRACKET,    CLOSE_BRACKET,    CLOSE_BRACKET,    ASTERISK,       ASTERISK,       ASTERISK,       ASTERISK,       Keys.ENTER,      Keys.ENTER,       Keys.ENTER,       Keys.ENTER },
              { PIPE,                PIPE,                PIPE,                PIPE,                PIPE,              PIPE,              Keys.COMMA,        Keys.COMMA,        Keys.COMMA,        Keys.COMMA,        Keys.MINUS,        Keys.MINUS,         Keys.MINUS,         Keys.MINUS,         Keys.PERIOD,        Keys.PERIOD, Keys.PERIOD, Keys.PERIOD, Keys.SLASH,  Keys.SLASH,  Keys.SLASH,  Keys.SLASH,      COLON,           COLON,           COLON,           COLON,      Keys.SEMICOLON, Keys.SEMICOLON, Keys.SEMICOLON, Keys.SEMICOLON,  LESS_THAN,       LESS_THAN,       LESS_THAN,       LESS_THAN,      GREATER_THAN,   GREATER_THAN,     GREATER_THAN,     GREATER_THAN,     QUESTION_MARK,    QUESTION_MARK,    QUESTION_MARK,    QUESTION_MARK,  Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER,      Keys.ENTER,       Keys.ENTER,       Keys.ENTER },
              { Keys.BACKSLASH,      Keys.BACKSLASH,      Keys.BACKSLASH,      Keys.BACKSLASH,      Keys.BACKSLASH,    Keys.BACKSLASH,    Keys.BACKSLASH,    Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, CARAT,       CARAT,       CARAT,       CARAT,       UNDERSCORE,  UNDERSCORE,  UNDERSCORE,      UNDERSCORE,      OPEN_BRACE,      OPEN_BRACE,      OPEN_BRACE, OPEN_BRACE,     CLOSE_BRACE,    CLOSE_BRACE,    CLOSE_BRACE,     CLOSE_BRACE,     TILDE,           TILDE,           TILDE,          TILDE,          Keys.INSERT,      Keys.INSERT,      Keys.INSERT,      Keys.INSERT,      Keys.INSERT,      Keys.INSERT,      Keys.PAGE_UP,   Keys.PAGE_UP,   Keys.PAGE_UP,   Keys.PAGE_UP,   Keys.FORWARD_DEL,Keys.FORWARD_DEL, Keys.FORWARD_DEL, Keys.FORWARD_DEL },
              { Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.ALT_LEFT,     Keys.ALT_LEFT,      Keys.ALT_LEFT,      Keys.ALT_LEFT,      Keys.SPACE,         Keys.SPACE,  Keys.SPACE,  Keys.SPACE,  Keys.SPACE,  Keys.SPACE,  Keys.SPACE,  Keys.SPACE,      Keys.SPACE,      Keys.SPACE,      Keys.SPACE,      Keys.SPACE, Keys.SPACE,     Keys.SPACE,     Keys.SPACE,     Keys.SPACE,      Keys.SPACE,      Keys.SPACE,      Keys.SPACE,      Keys.AT,        Keys.AT,        Keys.AT,          Keys.AT,          Keys.HOME,        Keys.HOME,        Keys.HOME,        Keys.HOME,        Keys.PAGE_DOWN, Keys.PAGE_DOWN, Keys.PAGE_DOWN, Keys.PAGE_DOWN, Keys.END,        Keys.END,         Keys.END,         Keys.END }
            },
            "png/landscape_keyboard_punc_numbers.png",
            0.4f,
            110,
            0,
            176,
            1568,
            0
          ),
    PORTRAIT_LOWER_CASE(
            new Integer[][] {
              { Keys.ESCAPE,         Keys.ESCAPE,         Keys.ESCAPE,         Keys.ESCAPE,         Keys.F1,           Keys.F1,           Keys.F1,           Keys.F1,           Keys.F2,           Keys.F2,           Keys.F2,       Keys.F2,       Keys.F3,       Keys.F3,       Keys.F3,    Keys.F3,    Keys.F4,    Keys.F4,    Keys.F4,    Keys.F4,    Keys.F5,    Keys.F5,    Keys.F5,    Keys.F5,    Keys.F6,    Keys.F6,    Keys.F6,    Keys.F6,    Keys.F7,    Keys.F7,    Keys.F7,    Keys.F7,    Keys.F8,    Keys.F8,        Keys.F8,        Keys.F8,          Keys.F9,          Keys.F9,          Keys.F9,          Keys.F9,          Keys.F10,         Keys.F10,  Keys.F10,   Keys.F10,   Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE },
              { Keys.TAB,            Keys.TAB,            Keys.TAB,            Keys.TAB,            Keys.TAB,          Keys.Q,            Keys.Q,            Keys.Q,            Keys.Q,            Keys.W,            Keys.W,        Keys.W,        Keys.W,        Keys.E,        Keys.E,     Keys.E,     Keys.E,     Keys.R,     Keys.R,     Keys.R,     Keys.R,     Keys.T,     Keys.T,     Keys.T,     Keys.T,     Keys.Y,     Keys.Y,     Keys.Y,     Keys.Y,     Keys.U,     Keys.U,     Keys.U,     Keys.U,     Keys.I,         Keys.I,         Keys.I,           Keys.I,           Keys.O,           Keys.O,           Keys.O,           Keys.O,           Keys.P,    Keys.P,     Keys.P,     Keys.P,         Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER },
              { Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,    Keys.CAPS_LOCK,    Keys.A,            Keys.A,            Keys.A,            Keys.A,            Keys.S,        Keys.S,        Keys.S,        Keys.S,        Keys.D,     Keys.D,     Keys.D,     Keys.D,     Keys.F,     Keys.F,     Keys.F,     Keys.F,     Keys.G,     Keys.G,     Keys.G,     Keys.G,     Keys.H,     Keys.H,     Keys.H,     Keys.H,     Keys.J,     Keys.J,     Keys.J,     Keys.J,         Keys.K,         Keys.K,           Keys.K,           Keys.K,           Keys.L,           Keys.L,           Keys.L,           Keys.L,    Keys.ENTER, Keys.ENTER, Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER },
              { Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,   Keys.SHIFT_LEFT,   Keys.SHIFT_LEFT,   Keys.Z,            Keys.Z,            Keys.Z,            Keys.Z,        Keys.X,        Keys.X,        Keys.X,        Keys.X,     Keys.C,     Keys.C,     Keys.C,     Keys.C,     Keys.V,     Keys.V,     Keys.V,     Keys.V,     Keys.B,     Keys.B,     Keys.B,     Keys.B,     Keys.N,     Keys.N,     Keys.N,     Keys.N,     Keys.M,     Keys.M,     Keys.M,         Keys.M,         Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.UP,   Keys.UP,    Keys.UP,    Keys.UP,        Keys.EQUALS,    Keys.EQUALS,    Keys.EQUALS,    Keys.EQUALS },
              { Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.ALT_RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT,   Keys.ALT_RIGHT,   Keys.LEFT,        Keys.LEFT,        Keys.LEFT,        Keys.LEFT,        Keys.DOWN, Keys.DOWN,  Keys.DOWN,  Keys.DOWN,      Keys.RIGHT,     Keys.RIGHT,     Keys.RIGHT,     Keys.RIGHT }
            },
            "png/portrait_keyboard_lowercase.png",
            0.6f,
            135,
            0,
            1,
            -1,
            0
          ),
    PORTRAIT_UPPER_CASE(
            new Integer[][] {
              { Keys.ESCAPE,         Keys.ESCAPE,         Keys.ESCAPE,         Keys.ESCAPE,         Keys.F1,           Keys.F1,           Keys.F1,           Keys.F1,           Keys.F2,           Keys.F2,           Keys.F2,       Keys.F2,       Keys.F3,       Keys.F3,       Keys.F3,    Keys.F3,    Keys.F4,    Keys.F4,    Keys.F4,    Keys.F4,    Keys.F5,    Keys.F5,    Keys.F5,    Keys.F5,    Keys.F6,    Keys.F6,    Keys.F6,    Keys.F6,    Keys.F7,    Keys.F7,    Keys.F7,    Keys.F7,    Keys.F8,    Keys.F8,        Keys.F8,        Keys.F8,          Keys.F9,          Keys.F9,          Keys.F9,          Keys.F9,          Keys.F10,         Keys.F10,  Keys.F10,   Keys.F10,   Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE, Keys.BACKSPACE },
              { Keys.TAB,            Keys.TAB,            Keys.TAB,            Keys.TAB,            Keys.TAB,          UPPER_Q,           UPPER_Q,           UPPER_Q,           UPPER_Q,           UPPER_W,           UPPER_W,       UPPER_W,       UPPER_W,       UPPER_E,       UPPER_E,    UPPER_E,    UPPER_E,    UPPER_R,    UPPER_R,    UPPER_R,    UPPER_R,    UPPER_T,    UPPER_T,    UPPER_T,    UPPER_T,    UPPER_Y,    UPPER_Y,    UPPER_Y,    UPPER_Y,    UPPER_U,    UPPER_U,    UPPER_U,    UPPER_U,    UPPER_I,        UPPER_I,        UPPER_I,          UPPER_I,          UPPER_O,          UPPER_O,          UPPER_O,          UPPER_O,          UPPER_P,   UPPER_P,    UPPER_P,    UPPER_P,        Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER },
              { Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,      Keys.CAPS_LOCK,    Keys.CAPS_LOCK,    UPPER_A,           UPPER_A,           UPPER_A,           UPPER_A,           UPPER_S,       UPPER_S,       UPPER_S,       UPPER_S,       UPPER_D,    UPPER_D,    UPPER_D,    UPPER_D,    UPPER_F,    UPPER_F,    UPPER_F,    UPPER_F,    UPPER_G,    UPPER_G,    UPPER_G,    UPPER_G,    UPPER_H,    UPPER_H,    UPPER_H,    UPPER_H,    UPPER_J,    UPPER_J,    UPPER_J,    UPPER_J,        UPPER_K,        UPPER_K,          UPPER_K,          UPPER_K,          UPPER_L,          UPPER_L,          UPPER_L,          UPPER_L,   Keys.ENTER, Keys.ENTER, Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER },
              { Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,     Keys.SHIFT_LEFT,   Keys.SHIFT_LEFT,   Keys.SHIFT_LEFT,   UPPER_Z,           UPPER_Z,           UPPER_Z,           UPPER_Z,       UPPER_X,       UPPER_X,       UPPER_X,       UPPER_X,    UPPER_C,    UPPER_C,    UPPER_C,    UPPER_C,    UPPER_V,    UPPER_V,    UPPER_V,    UPPER_V,    UPPER_B,    UPPER_B,    UPPER_B,    UPPER_B,    UPPER_N,    UPPER_N,    UPPER_N,    UPPER_N,    UPPER_M,    UPPER_M,    UPPER_M,        UPPER_M,        Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.UP,   Keys.UP,    Keys.UP,    Keys.UP,        Keys.EQUALS,    Keys.EQUALS,    Keys.EQUALS,    Keys.EQUALS },
              { Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.ALT_LEFT, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.ALT_RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT,   Keys.ALT_RIGHT,   Keys.LEFT,        Keys.LEFT,        Keys.LEFT,        Keys.LEFT,        Keys.DOWN, Keys.DOWN,  Keys.DOWN,  Keys.DOWN,      Keys.RIGHT,     Keys.RIGHT,     Keys.RIGHT,     Keys.RIGHT }
            },
            "png/portrait_keyboard_uppercase.png",
            0.6f,
            135,
            0,
            1,
            -1,
            0
          ),
    PORTRAIT_PUNC_NUMBERS(
            new Integer[][] {
              { Keys.GRAVE,          Keys.GRAVE,          Keys.GRAVE,          Keys.GRAVE,          Keys.NUM_1,        Keys.NUM_1,        Keys.NUM_1,        Keys.NUM_1,        Keys.NUM_2,        Keys.NUM_2,        Keys.NUM_2,        Keys.NUM_2,         Keys.NUM_3,         Keys.NUM_3,         Keys.NUM_3,         Keys.NUM_3,  Keys.NUM_4,  Keys.NUM_4,  Keys.NUM_4,  Keys.NUM_4,  Keys.NUM_5,  Keys.NUM_5,      Keys.NUM_5,      Keys.NUM_5,      Keys.NUM_6,      Keys.NUM_6, Keys.NUM_6,     Keys.NUM_6,     Keys.NUM_7,     Keys.NUM_7,      Keys.NUM_7,      Keys.NUM_7,      Keys.NUM_8,      Keys.NUM_8,     Keys.NUM_8,     Keys.NUM_8,       Keys.NUM_9,       Keys.NUM_9,       Keys.NUM_9,       Keys.NUM_9,       Keys.NUM_0,       Keys.NUM_0,     Keys.NUM_0,     Keys.NUM_0,     Keys.BACKSPACE, Keys.BACKSPACE,  Keys.BACKSPACE,   Keys.BACKSPACE,   Keys.BACKSPACE },
              { Keys.PLUS,           Keys.PLUS,           Keys.PLUS,           Keys.PLUS,           Keys.PLUS,         EXCLAIMATION_MARK, EXCLAIMATION_MARK, EXCLAIMATION_MARK, EXCLAIMATION_MARK, DOUBLE_QUOTE,      DOUBLE_QUOTE,      DOUBLE_QUOTE,       DOUBLE_QUOTE,       HASH,               HASH,               HASH,        HASH,        DOLLAR_SIGN, DOLLAR_SIGN, DOLLAR_SIGN, DOLLAR_SIGN, PERCENTAGE_SIGN, PERCENTAGE_SIGN, PERCENTAGE_SIGN, PERCENTAGE_SIGN, AMPERSAND,  AMPERSAND,      AMPERSAND,      AMPERSAND,      Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.APOSTROPHE, OPEN_BRACKET,   OPEN_BRACKET,   OPEN_BRACKET,     OPEN_BRACKET,     CLOSE_BRACKET,    CLOSE_BRACKET,    CLOSE_BRACKET,    CLOSE_BRACKET,    ASTERISK,       ASTERISK,       ASTERISK,       ASTERISK,       Keys.ENTER,      Keys.ENTER,       Keys.ENTER,       Keys.ENTER },
              { PIPE,                PIPE,                PIPE,                PIPE,                PIPE,              PIPE,              Keys.COMMA,        Keys.COMMA,        Keys.COMMA,        Keys.COMMA,        Keys.MINUS,        Keys.MINUS,         Keys.MINUS,         Keys.MINUS,         Keys.PERIOD,        Keys.PERIOD, Keys.PERIOD, Keys.PERIOD, Keys.SLASH,  Keys.SLASH,  Keys.SLASH,  Keys.SLASH,      COLON,           COLON,           COLON,           COLON,      Keys.SEMICOLON, Keys.SEMICOLON, Keys.SEMICOLON, Keys.SEMICOLON,  LESS_THAN,       LESS_THAN,       LESS_THAN,       LESS_THAN,      GREATER_THAN,   GREATER_THAN,     GREATER_THAN,     GREATER_THAN,     QUESTION_MARK,    QUESTION_MARK,    QUESTION_MARK,    QUESTION_MARK,  Keys.ENTER,     Keys.ENTER,     Keys.ENTER,     Keys.ENTER,      Keys.ENTER,       Keys.ENTER,       Keys.ENTER },
              { Keys.BACKSLASH,      Keys.BACKSLASH,      Keys.BACKSLASH,      Keys.BACKSLASH,      Keys.BACKSLASH,    Keys.BACKSLASH,    Keys.BACKSLASH,    Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, CARAT,       CARAT,       CARAT,       CARAT,       UNDERSCORE,  UNDERSCORE,  UNDERSCORE,      UNDERSCORE,      OPEN_BRACE,      OPEN_BRACE,      OPEN_BRACE, OPEN_BRACE,     CLOSE_BRACE,    CLOSE_BRACE,    CLOSE_BRACE,     CLOSE_BRACE,     TILDE,           TILDE,           TILDE,          TILDE,          Keys.INSERT,      Keys.INSERT,      Keys.INSERT,      Keys.INSERT,      Keys.INSERT,      Keys.INSERT,      Keys.PAGE_UP,   Keys.PAGE_UP,   Keys.PAGE_UP,   Keys.PAGE_UP,   Keys.FORWARD_DEL,Keys.FORWARD_DEL, Keys.FORWARD_DEL, Keys.FORWARD_DEL },
              { Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.SWITCH_CHARSET, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.ALT_LEFT,     Keys.ALT_LEFT,      Keys.ALT_LEFT,      Keys.ALT_LEFT,      Keys.SPACE,         Keys.SPACE,  Keys.SPACE,  Keys.SPACE,  Keys.SPACE,  Keys.SPACE,  Keys.SPACE,  Keys.SPACE,      Keys.SPACE,      Keys.SPACE,      Keys.SPACE,      Keys.SPACE, Keys.SPACE,     Keys.SPACE,     Keys.SPACE,     Keys.SPACE,      Keys.SPACE,      Keys.SPACE,      Keys.SPACE,      Keys.AT,        Keys.AT,        Keys.AT,          Keys.AT,          Keys.HOME,        Keys.HOME,        Keys.HOME,        Keys.HOME,        Keys.PAGE_DOWN, Keys.PAGE_DOWN, Keys.PAGE_DOWN, Keys.PAGE_DOWN, Keys.END,        Keys.END,         Keys.END,         Keys.END }
            },
            "png/portrait_keyboard_punc_numbers.png",
            0.6f,
            135,
            0,
            1,
            -1,
            0
          ),
    MOBILE_ON_SCREEN,
    OFF;

    /**
     * The vertical size of the keys in this KeyboardType.
     */
    private float vertKeySize;

    /**
     * The horizontal size of the keys in this KeyboardType.
     */
    private float horizKeySize;

    /**
     * The position of each key within this KeyboardType.
     */
    private Integer[][] keyMap;

    /**
     * The Texture holding the keyboard image for this KeyboardType.
     */
    private Texture texture;

    /**
     * The opacity of this KeyboardType.
     */
    private float opacity;

    /**
     * Offset from the bottom of the screen that the keyboard is rendered at.
     */
    private int renderOffset;

    /**
     * The Y value above which the keyboard will be closed.
     */
    private int closeHeight;

    /**
     * The X value at which the keyboard starts in the keyboard image.
     */
    private int xStart;

    /**
     * The Y value at which the keyboard starts in the keyboard image.
     */
    private int yStart;

    /**
     * The width of the active part of the keyboard image, or -1 to deduce from texture width and xStart.
     */
    private int activeWidth;
    
    /**
     * Constructor for KeyboardType.
     * 
     * @param keyMap         The position of each key within this KeyboardType.
     * @param keyboardImage  The path to the keyboard image file.
     * @param opacity        The opacity of this KeyboardType.
     * @param renderOffset   Offset from the bottom of the screen that the keyboard
     *                       is rendered at.
     * @param closeBuffer    Buffer over the keyboard above which a tap or click
     *                       will close the keyboard.
     * @param xStart         The X value at which the keyboard starts in the
     *                       keyboard image.
     * @param activeWidth    The width of the active part of the keyboard image, or
     *                       -1 to deduce from texture width and xStart.
     * @param yStart         The Y value at which the keyboard starts in the
     *                       keyboard image.
     */
    KeyboardType(Integer[][] keyMap, String keyboardImage, float opacity, int renderOffset, int closeBuffer,
            int xStart, int activeWidth, int yStart) {
        this.keyMap = keyMap;
        this.texture = new Texture(keyboardImage);
        this.xStart = xStart;
        this.yStart = yStart;
        
        activeWidth = (activeWidth == -1 ? this.texture.getWidth() - this.xStart : activeWidth);
        
        this.vertKeySize = ((float) (((float) this.texture.getHeight()) - (float) this.yStart)
                / (float) this.keyMap.length);
        this.horizKeySize = ((float) activeWidth / (float) this.keyMap[0].length);
        this.opacity = opacity;
        this.renderOffset = renderOffset;
        this.closeHeight = this.texture.getHeight() + renderOffset + closeBuffer;
        this.activeWidth = activeWidth;
    }

    /**
     * Variant of the Constructor that doesn't support any key mapping, or visual
     * appearance
     */
    KeyboardType() {
    }

    /**
     * Gets the keycode that is mapped to the given X and Y world coordinates.
     * Returns null if there is no matching key at the given position.
     * 
     * @param x The X position within this KeyboardType's world coordinates.
     * @param y The Y position within this KeyboardType's world coordinates.
     * 
     * @return The keycode that is mapped to the given X and Y world coordinates, or
     *         null if there is not match.
     */
    public Integer getKeyCode(float x, float y) {
        Integer keyCode = null;
        int keyRow = (int) ((texture.getHeight() - (y - yStart) + renderOffset) / vertKeySize);

        if (keyRow >= keyMap.length) {
            keyRow = keyMap.length - 1;
        }

        switch (this) {
            case LANDSCAPE_LOWER_CASE:
            case LANDSCAPE_UPPER_CASE:
            case LANDSCAPE_PUNC_NUMBERS:
            case PORTRAIT_LOWER_CASE:
            case PORTRAIT_UPPER_CASE:
            case PORTRAIT_PUNC_NUMBERS:
                if (x >= xStart) {
                    keyCode = keyMap[keyRow][(int) ((x - xStart) / horizKeySize)];
                }
                break;
    
            default:
                break;
        }
        
        return keyCode;
    }

    /**
     * Tests if the given X/Y position is within the bounds of this KeyboardTypes
     * keyboard image.
     * 
     * @param x The X position to test.
     * @param y The Y position to test.
     * 
     * @return true if the given X/Y position is within the keyboard image;
     *         otherwise false.
     */
    public boolean isInKeyboard(float x, float y) {
        if (isRendered()) {
            boolean isInYBounds = (y < (texture.getHeight() + renderOffset) && (y > renderOffset));
            boolean isInXBounds = ((x >= xStart) && (x < (xStart + activeWidth)));
            return isInYBounds && isInXBounds;
            
        } else {
            // isInKeyboard only applies to rendered keyboards.
            return false;
        }
    }

    /**
     * @return The Texture holding the keyboard image for this KeyboardType.
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * @return The opacity of this KeyboardType.
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * @return true if this KeyboardType is rendered by the AGILE render code;
     *         otherwise false.
     */
    public boolean isRendered() {
        return (texture != null);
    }
    
    /**
     * @return true if this KeyboardType is a landscape keyboard, otherwise false.
     */
    public boolean isLandscape() {
        return (equals(LANDSCAPE_LOWER_CASE) ||
                equals(LANDSCAPE_UPPER_CASE) || 
                equals(LANDSCAPE_PUNC_NUMBERS));
    }
    
    /**
     * @return true if this KeyboardType is a portrait keyboard, otherwise false.
     */
    public boolean isPortrait() {
        return (equals(PORTRAIT_LOWER_CASE) ||
                equals(PORTRAIT_UPPER_CASE) || 
                equals(PORTRAIT_PUNC_NUMBERS));
    }

    /**
     * @return Offset from the bottom of the screen that the keyboard is rendered
     *         at.
     */
    public int getRenderOffset() {
        return renderOffset;
    }

    /**
     * @return The height above which the keyboard will close.
     */
    public int getCloseHeight() {
        return closeHeight;
    }

    /**
     * Disposes of the libGDX Texture for all KeyboardTypes.
     */
    public static void dispose() {
        for (KeyboardType keyboardType : KeyboardType.values()) {
            if (keyboardType.texture != null) {
                keyboardType.texture.dispose();
                keyboardType.texture = null;
            }
        }
    }
}
