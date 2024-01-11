package com.agifans.agile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ScriptBuffer {

    public enum ScriptBufferEventType {
        LOAD_LOGIC,
        LOAD_VIEW,
        LOAD_PIC,
        LOAD_SOUND,
        DRAW_PIC,
        ADD_TO_PIC,
        DISCARD_PIC,
        DISCARD_VIEW,
        OVERLAY_PIC
    }

    public class ScriptBufferEvent {
        public ScriptBufferEventType type;
        public int resourceNumber;
        public byte[] data;

        public ScriptBufferEvent(ScriptBufferEventType type, int resourceNumber, byte[] data) {
            this.type = type;
            this.resourceNumber = resourceNumber;
            this.data = data;
        }
    }

    /**
     * The GameState class holds all of the data and state for the Game currently 
     * being run by the interpreter.
     */
    private GameState state;

    /**
     * A transcript of events leading to the current state in the current room.
     */
    public List<ScriptBufferEvent> events;

    /**
     * Whether or not the storage of script events in the buffer is enabled or not.
     */
    private boolean doScript;

    public int maxScript;
    public int scriptSize;
    public int scriptEntries() {
        int count = 0;
        for (ScriptBufferEvent e : events)
        {
            // in AGI, the add.to.pic script event consist of 4 entries
            // (who, action, loop #, view #, X, Y, cel #, priority)
            // the rest of the events are just 1 entry (who, action)
            if (e.type == ScriptBufferEventType.ADD_TO_PIC)
            {
                count += 4;
            }
            else
            {
                count += 1;
            }
        }
        return count;
    }
    public int savedScript;

    /**
     * Constructor for ScriptBuffer.
     * 
     * @param state
     */
    public ScriptBuffer(GameState state) {
        // Default script size is 50 according to original AGI specs.
        this.scriptSize = 50;
        this.events = new ArrayList<ScriptBufferEvent>();
        this.state = state;
        initScript();
    }

    /**
     *
     */
    public void scriptOff() {
        doScript = false;
    }

    /**
     *
     */
    public void scriptOn() {
        doScript = true;
    }

    /**
     * Initialize the script buffer.
     */
    public void initScript() {
        events.clear();
    }

    /**
     * Add an event to the script buffer
     * 
     * @param action
     * @param who
     */
    public void addScript(ScriptBufferEventType action, int who) {
        addScript(action, who, null);
    }
    
    /**
     * Add an event to the script buffer
     *
     * @param action
     * @param who
     * @param data
     */
    public void addScript(ScriptBufferEventType action, int who, byte[] data) {
        if (state.getFlag(Defines.NO_SCRIPT)) return;

        if (doScript) {
            if (events.size() >= this.scriptSize) {
                // TODO: Error. Error(11, maxScript);
                return;
            }
            else {
                events.add(new ScriptBufferEvent(action, who, data));
            }
        }

        if (events.size() > maxScript) {
            maxScript = events.size();
        }
    }

    /**
     *  
     * @param scriptSize
     */
    public void setScriptSize(int scriptSize) {
        this.scriptSize = scriptSize;
        this.events.clear();
    }

    /**
     *
     */
    public void pushScript() {
        this.savedScript = events.size();
    }

    /**
     *
     */
    public void popScript() {
        if (events.size() > this.savedScript) {
            events = events.subList(0, this.savedScript);
        }
    }

    /**
     * Returns the script event buffer as a raw byte array.
     *
     * @return
     */
    public byte[] encode() {
        // Each script entry is two bytes long.
        ByteArrayOutputStream stream = new ByteArrayOutputStream(this.scriptSize * 2);

        for (ScriptBufferEvent e : events) {
            stream.write((byte)(e.type.ordinal()));
            stream.write((byte)e.resourceNumber);
            if (e.data != null) {
                stream.write(e.data, 0, e.data.length);
            }
        }
        
        // If we didn't write exactly the expected size, then fill the rest with 0.
        while (stream.size() < (this.scriptSize * 2)) {
            stream.write(0);
        }

        return stream.toByteArray();
    }

    /**
     * Add an event to the script buffer without checking NO_SCRIPT flag. Used primarily by restore save game function.
     * 
     * @param action
     * @param who
     */
    public void restoreScript(ScriptBufferEventType action, int who) {
        restoreScript(action, who, null);
    }
    
    /**
     * Add an event to the script buffer without checking NO_SCRIPT flag. Used primarily by restore save game function.
     *
     * @param action
     * @param who 
     */
    public void restoreScript(ScriptBufferEventType action, int who, byte[] data) {
        events.add(new ScriptBufferEvent(action, who, data));

        if (events.size() > maxScript) {
            maxScript = events.size();
        }
    }
}
