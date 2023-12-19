package com.agifans.agile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

/**
 * Handles the input of keyboard events, mapping them to a form that the AGILE
 * interpreter can query as required. 
 */
public class UserInput extends InputAdapter {

    /**
     * The SHIFT modifier key.
     */
    private static final int SHIFT_MODIFIER = 0x10000;
    
    /**
     * The CTRL modifier key.
     */
    private static final int CONTROL_MODIFIER = 0x20000;
    
    /**
     * The ALT modifier key.
     */
    private static final int ALT_MODIFIER = 0x40000;
    
    /**
     * Marks the enqueued keycode value as an ASCII character.
     */
    public static final int ASCII = 0x80000;
    
    // AGI ACCEPT/ABORT input values.
    public static final int ACCEPT = 0;
    public static final int ABORT = 1;
    
    /**
     * A queue of all key presses that the user has made.
     */
    public ConcurrentLinkedQueue<Integer> keyPressQueue;

    /**
     * Current state of every key on the keyboard.
    */
    public boolean[] keys;

    /**
     * Stores the state of every key on the previous cycle.
     */
    public boolean[] oldKeys;

    /**
     * Current state of the ALT/SHIFT/CONTROL modifiers, as bit mask.
     */
    private int modifiers;
    
    /**
     * A Map between IBM PC key codes as understood by the PC AGI interpreter and the C# Key codes.
     */
    public Map<Integer, Integer> keyCodeMap;

    public Map<Integer, Integer> reverseKeyCodeMap;

    /**
     * Unmodified LibGDX key values that we will enqueue as-is.
     */
    private static final List<Integer> UNMODIFIED_KEY_LIST = Arrays.asList(
            Keys.F1,
            Keys.F2,
            Keys.F3,
            Keys.F4,
            Keys.F5,
            Keys.F6,
            Keys.F7,
            Keys.F8,
            Keys.F9,
            Keys.F10,
            Keys.HOME,
            Keys.UP,
            Keys.PAGE_UP,
            Keys.LEFT,
            Keys.RIGHT,
            Keys.END,
            Keys.DOWN,
            Keys.PAGE_DOWN,
            Keys.HOME,
            Keys.INSERT, 
            Keys.DEL);
    
    /**
     * Constructor for UserInput.
     */
    public UserInput() {
        this.keys = new boolean[256];
        this.oldKeys = new boolean[256];
        this.keyPressQueue = new ConcurrentLinkedQueue<Integer>();
        this.keyCodeMap = createKeyConversionMap();
        this.reverseKeyCodeMap = new HashMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> entry : keyCodeMap.entrySet()) {
            if (!reverseKeyCodeMap.containsKey(entry.getValue()) && (entry.getValue() != 0)) {
                reverseKeyCodeMap.put(entry.getValue(), entry.getKey());
            }
        }
    }

    /**
     * Handles the key down event.
     * 
     * @param keycode one of the constants in {@link Character.Keys}
     * 
     * @return whether the input was processed 
     */
    public boolean keyDown (int keycode) {
        //System.out.println(String.format("keyDown: 0x%04X [modifiers=0x%05X]", 
        //        (int)keycode, modifiers));
        
        // AGILE interpreter ignores some keys completely, e.g. F11.
        if (keycode == Keys.F11) {
            return false;
        }

        this.keys[keycode & 0xFF] = true;

        // Update modifies for ALT/SHIFT/CONTROL but do not enqueue key presses that 
        // are Alt/Shift/Ctrl by themselves. AGI doesn't support mapping those.
        if ((keycode == Keys.SHIFT_LEFT) || (keycode == Keys.SHIFT_RIGHT)) {
            return true;
        }
        if ((keycode == Keys.ALT_LEFT) || (keycode == Keys.ALT_RIGHT)) {
            modifiers |= ALT_MODIFIER;
            return true;
        }
        if ((keycode == Keys.CONTROL_LEFT) || (keycode == Keys.CONTROL_RIGHT)) {
            modifiers |= CONTROL_MODIFIER;
            return true;
        }

        // Some keys and key combinations we need to map to ASCII characters.
        Integer character = Character.KEYSTROKE_TO_CHAR_MAP.get(modifiers + keycode);
        if (character != null) {
            // Enqueue the mapped character. This covers CTRL combinations and ESC.
            keyPressQueue.add(ASCII | character);
        } 
        else if (modifiers != 0) {
            // Enqueues the ALT combinations.
            keyPressQueue.add(modifiers | keycode);
        } 
        else if (UNMODIFIED_KEY_LIST.contains(keycode)) {
            // Any other keycode that didn't map to a character, and isn't affected
            // by modifiers, is enqueued as-is.
            keyPressQueue.add(keycode);
        }
        
        return true;
    }

    /**
     * Handles the key up event.
     */
    public boolean keyUp(int keycode) {
        //System.out.println(String.format("keyUp: 0x%04X [modifiers=0x%05X]", 
        //        (int)keycode, modifiers));
        
        this.keys[keycode & 0xFF] = false;
        
        // Update modifiers for ALT/CONTROL
        if ((keycode == Keys.ALT_LEFT) || (keycode == Keys.ALT_RIGHT)) {
            modifiers &= (~ALT_MODIFIER);
            return true;
        }
        if ((keycode == Keys.CONTROL_LEFT) || (keycode == Keys.CONTROL_RIGHT)) {
            modifiers &= (~CONTROL_MODIFIER);
            return true;
        }
        
        return true;
    }

    /**
     * Handles the key pressed event.
     * 
     * @param character The character that was typed.
     */
    public boolean keyTyped(char character) {
        //System.out.println(String.format("keyTyped: 0x%02X [modifiers=0x%05X]", 
        //        (int)character, modifiers));
        
        // NOTE: The keyTyped method isn't invoked when ALT and CTRL are used.

        // We handle ENTER ourselves in keyDown, via the HashMap in Character class.
        if ((character != 0x0A) && (character != 0x0D)) {
            keyPressQueue.add(ASCII | (int)character);
        }
        
        return true;
    }

    /**
     * Wait for and return either ACCEPT or ABORT.
     * 
     * @return Either ACCEPT or ABORT, depending on what was chosen.
     */
    public int waitAcceptAbort() {
        int action;

        // Ignore anything currently on the key press queue.
        while (keyPressQueue.poll() != null) ;

        // Now wait for the the next key.
        while ((action = checkAcceptAbort()) == -1) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // Ignore.
            }
        }

        return action;
    }

    /**
     * Waits for the next key to be pressed then returns the value. Always clears
     * the key press queue beforehand.
     * 
     * @return The key that was pressed.
     */
    public int waitForKey() {
        return waitForKey(true);
    }
    
    /**
     * Waits for the next key to be pressed then returns the value.
     * 
     * @param clearQueue Whether to clear what is on the queue before waiting.
     * 
     * @returnThe key that was pressed.
     */
    public int waitForKey(boolean clearQueue) {
        int key;

        if (clearQueue) {
            // Ignore anything currently on the key press queue.
            while (keyPressQueue.poll() != null) ;
        }

        // Now wait for the the next key.
        while ((key = getKey()) == 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // Ignore.
            }
        }
        
        return key;
    }

    /**
     * Check if either ACCEPT or ABORT has been selected. Return the value if so, -1 otherwise.
     * 
     * @return Either ACCEPT or ABORT; otherwise -1 if neither was selected.
     */
    public int checkAcceptAbort() {
        int c;

        if ((c = getKey()) == (ASCII | Character.ENTER)) {
            return ACCEPT;
        }
        else if (c == (ASCII | Character.ESC)) {
            return ABORT;
        }
        else {
            return -1;
        }
    }

    /**
     * Gets a key from the key queue. Return 0 if none available.
     * 
     * @return Either the key from the queue, or 0 if none available.
     */
    public int getKey() {
        return (keyPressQueue.peek() != null? keyPressQueue.poll() : 0);
    }

    /**
     * Creates the Map between key codes as understood by the PC AGI interpreter 
     * and the libGDX Key codes.
     */
    private Map<Integer, Integer> createKeyConversionMap() {
        Map<Integer, Integer> controllerMap = new HashMap<>();

        controllerMap.put(9, ASCII | Character.TAB);
        controllerMap.put(27, ASCII | Character.ESC);
        controllerMap.put(13, ASCII | Character.ENTER);

        // Function keys.
        controllerMap.put((59 << 8) + 0, Keys.F1);
        controllerMap.put((60 << 8) + 0, Keys.F2);
        controllerMap.put((61 << 8) + 0, Keys.F3);
        controllerMap.put((62 << 8) + 0, Keys.F4);
        controllerMap.put((63 << 8) + 0, Keys.F5);
        controllerMap.put((64 << 8) + 0, Keys.F6);
        controllerMap.put((65 << 8) + 0, Keys.F7);
        controllerMap.put((66 << 8) + 0, Keys.F8);
        controllerMap.put((67 << 8) + 0, Keys.F9);
        controllerMap.put((68 << 8) + 0, Keys.F10);

        // Control and another key.
        controllerMap.put(1, ASCII | Character.CTRL_A);
        controllerMap.put(2, ASCII | Character.CTRL_B);
        controllerMap.put(3, ASCII | Character.CTRL_C);
        controllerMap.put(4, ASCII | Character.CTRL_D);
        controllerMap.put(5, ASCII | Character.CTRL_E);
        controllerMap.put(6, ASCII | Character.CTRL_F);
        controllerMap.put(7, ASCII | Character.CTRL_G);
        controllerMap.put(8, ASCII | Character.CTRL_H);
        controllerMap.put(10, ASCII | Character.CTRL_J);
        controllerMap.put(11, ASCII | Character.CTRL_K);
        controllerMap.put(12, ASCII | Character.CTRL_L);
        controllerMap.put(14, ASCII | Character.CTRL_N);
        controllerMap.put(15, ASCII | Character.CTRL_O);
        controllerMap.put(16, ASCII | Character.CTRL_P);
        controllerMap.put(17, ASCII | Character.CTRL_Q);
        controllerMap.put(18, ASCII | Character.CTRL_R);
        controllerMap.put(19, ASCII | Character.CTRL_S);
        controllerMap.put(20, ASCII | Character.CTRL_T);
        controllerMap.put(21, ASCII | Character.CTRL_U);
        controllerMap.put(22, ASCII | Character.CTRL_V);
        controllerMap.put(23, ASCII | Character.CTRL_W);
        controllerMap.put(24, ASCII | Character.CTRL_X);
        controllerMap.put(25, ASCII | Character.CTRL_Y);
        controllerMap.put(26, ASCII | Character.CTRL_Z);

        // Alt and another key.
        controllerMap.put((16 << 8) + 0, ALT_MODIFIER | Keys.Q);
        controllerMap.put((17 << 8) + 0, ALT_MODIFIER | Keys.W);
        controllerMap.put((18 << 8) + 0, ALT_MODIFIER | Keys.E);
        controllerMap.put((19 << 8) + 0, ALT_MODIFIER | Keys.R);
        controllerMap.put((20 << 8) + 0, ALT_MODIFIER | Keys.T);
        controllerMap.put((21 << 8) + 0, ALT_MODIFIER | Keys.Y);
        controllerMap.put((22 << 8) + 0, ALT_MODIFIER | Keys.U);
        controllerMap.put((23 << 8) + 0, ALT_MODIFIER | Keys.I);
        controllerMap.put((24 << 8) + 0, ALT_MODIFIER | Keys.O);
        controllerMap.put((25 << 8) + 0, ALT_MODIFIER | Keys.P);
        controllerMap.put((30 << 8) + 0, ALT_MODIFIER | Keys.A);
        controllerMap.put((31 << 8) + 0, ALT_MODIFIER | Keys.S);
        controllerMap.put((32 << 8) + 0, ALT_MODIFIER | Keys.D);
        controllerMap.put((33 << 8) + 0, ALT_MODIFIER | Keys.F);
        controllerMap.put((34 << 8) + 0, ALT_MODIFIER | Keys.G);
        controllerMap.put((35 << 8) + 0, ALT_MODIFIER | Keys.H);
        controllerMap.put((36 << 8) + 0, ALT_MODIFIER | Keys.J);
        controllerMap.put((37 << 8) + 0, ALT_MODIFIER | Keys.K);
        controllerMap.put((38 << 8) + 0, ALT_MODIFIER | Keys.L);
        controllerMap.put((44 << 8) + 0, ALT_MODIFIER | Keys.Z);
        controllerMap.put((45 << 8) + 0, ALT_MODIFIER | Keys.X);
        controllerMap.put((46 << 8) + 0, ALT_MODIFIER | Keys.C);
        controllerMap.put((47 << 8) + 0, ALT_MODIFIER | Keys.V);
        controllerMap.put((48 << 8) + 0, ALT_MODIFIER | Keys.B);
        controllerMap.put((49 << 8) + 0, ALT_MODIFIER | Keys.N);
        controllerMap.put((50 << 8) + 0, ALT_MODIFIER | Keys.M);

        controllerMap.put(28, ASCII | Character.CTRL_BACK_SLASH);
        controllerMap.put(29, ASCII | Character.CTRL_CLOSE_SQUARE_BRACKET);
        controllerMap.put(30, ASCII | Character.CTRL_6);
        controllerMap.put(31, ASCII | Character.CTRL_MINUS);
        
        // Normal printable chars.
        controllerMap.put(32, (ASCII | ' '));
        controllerMap.put(33, (ASCII | '!'));
        controllerMap.put(34, (ASCII | '"'));
        controllerMap.put(35, (ASCII | '#'));
        controllerMap.put(36, (ASCII | '$'));
        controllerMap.put(37, (ASCII | '%'));
        controllerMap.put(38, (ASCII | '&'));
        controllerMap.put(39, (ASCII | '\''));
        controllerMap.put(40, (ASCII | '('));
        controllerMap.put(41, (ASCII | ')'));
        controllerMap.put(42, (ASCII | '*'));
        controllerMap.put(43, (ASCII | '+'));
        controllerMap.put(44, (ASCII | ','));
        controllerMap.put(45, (ASCII | '-'));
        controllerMap.put(46, (ASCII | '.'));
        controllerMap.put(47, (ASCII | '/'));
        controllerMap.put(48, (ASCII | '0'));
        controllerMap.put(49, (ASCII | '1'));
        controllerMap.put(50, (ASCII | '2'));
        controllerMap.put(51, (ASCII | '3'));
        controllerMap.put(52, (ASCII | '4'));
        controllerMap.put(53, (ASCII | '5'));
        controllerMap.put(54, (ASCII | '6'));
        controllerMap.put(55, (ASCII | '7'));
        controllerMap.put(56, (ASCII | '8'));
        controllerMap.put(57, (ASCII | '9'));
        controllerMap.put(58, (ASCII | ':'));
        controllerMap.put(59, (ASCII | ';'));
        controllerMap.put(60, (ASCII | '<'));
        controllerMap.put(61, (ASCII | '='));
        controllerMap.put(62, (ASCII | '>'));
        controllerMap.put(63, (ASCII | '?'));
        controllerMap.put(64, (ASCII | '@'));

        // Manhunter games use unmodified alpha chars as controllers, e.g. C and S. AGI Demo Packs do as well.
        controllerMap.put(65, (ASCII | 'a'));
        controllerMap.put(66, (ASCII | 'b'));
        controllerMap.put(67, (ASCII | 'c'));
        controllerMap.put(68, (ASCII | 'd'));
        controllerMap.put(69, (ASCII | 'e'));
        controllerMap.put(70, (ASCII | 'f'));
        controllerMap.put(71, (ASCII | 'g'));
        controllerMap.put(72, (ASCII | 'h'));
        controllerMap.put(73, (ASCII | 'i'));
        controllerMap.put(74, (ASCII | 'j'));
        controllerMap.put(75, (ASCII | 'k'));
        controllerMap.put(76, (ASCII | 'l'));
        controllerMap.put(77, (ASCII | 'm'));
        controllerMap.put(78, (ASCII | 'n'));
        controllerMap.put(79, (ASCII | 'o'));
        controllerMap.put(80, (ASCII | 'p'));
        controllerMap.put(81, (ASCII | 'q'));
        controllerMap.put(82, (ASCII | 'r'));
        controllerMap.put(83, (ASCII | 's'));
        controllerMap.put(84, (ASCII | 't'));
        controllerMap.put(85, (ASCII | 'u'));
        controllerMap.put(86, (ASCII | 'v'));
        controllerMap.put(87, (ASCII | 'w'));
        controllerMap.put(88, (ASCII | 'x'));
        controllerMap.put(89, (ASCII | 'y'));
        controllerMap.put(90, (ASCII | 'z'));
        controllerMap.put(91, (ASCII | '['));
        controllerMap.put(92, (ASCII | '\\'));
        controllerMap.put(93, (ASCII | ']'));
        controllerMap.put(94, (ASCII | '^'));
        controllerMap.put(95, (ASCII | '_'));
        controllerMap.put(96, (ASCII | '`'));
        controllerMap.put(97,  (ASCII | 'A'));
        controllerMap.put(98,  (ASCII | 'B'));
        controllerMap.put(99,  (ASCII | 'C'));
        controllerMap.put(100, (ASCII | 'D'));
        controllerMap.put(101, (ASCII | 'E'));
        controllerMap.put(102, (ASCII | 'F'));
        controllerMap.put(103, (ASCII | 'G'));
        controllerMap.put(104, (ASCII | 'H'));
        controllerMap.put(105, (ASCII | 'I'));
        controllerMap.put(106, (ASCII | 'J'));
        controllerMap.put(107, (ASCII | 'K'));
        controllerMap.put(108, (ASCII | 'L'));
        controllerMap.put(109, (ASCII | 'M'));
        controllerMap.put(110, (ASCII | 'N'));
        controllerMap.put(111, (ASCII | 'O'));
        controllerMap.put(112, (ASCII | 'P'));
        controllerMap.put(113, (ASCII | 'Q'));
        controllerMap.put(114, (ASCII | 'R'));
        controllerMap.put(115, (ASCII | 'S'));
        controllerMap.put(116, (ASCII | 'T'));
        controllerMap.put(117, (ASCII | 'U'));
        controllerMap.put(118, (ASCII | 'V'));
        controllerMap.put(119, (ASCII | 'W'));
        controllerMap.put(120, (ASCII | 'X'));
        controllerMap.put(121, (ASCII | 'Y'));
        controllerMap.put(122, (ASCII | 'Z'));
        controllerMap.put(123, (ASCII | '{'));
        controllerMap.put(124, (ASCII | '|'));
        controllerMap.put(125, (ASCII | '}'));
        controllerMap.put(126, (ASCII | '~'));

        // Joysick codes. We're going to ignore these for now. Who uses a Joystick anyway? Maybe in the 80s. :)
        controllerMap.put((1 << 8) + 1, 0);
        controllerMap.put((1 << 8) + 2, 0);
        controllerMap.put((1 << 8) + 3, 0);
        controllerMap.put((1 << 8) + 4, 0);

        return controllerMap;
    }
}