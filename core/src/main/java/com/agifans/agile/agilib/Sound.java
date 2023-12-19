package com.agifans.agile.agilib;

import java.util.ArrayList;
import java.util.List;

public class Sound extends Resource {
    
    /**
     * The three tone channels.
     */
    public List<List<Note>> notes;

    byte[] rawData = null;

    /**
     * Constructor for Sound.
     * 
     * @param rawData The raw encoded AGI SOUND data for this Sound.
     */
    public Sound(byte[] rawData) {
        this.notes = new ArrayList<List<Note>>();

        for (int i=0; i < 4; i++)
        {
            this.notes.add(new ArrayList<Note>());
        }

        decode(rawData);
    }
    
    public void decode(byte[] rawData) {
        for (int n = 0; n < 4; n++) {
            int start = (rawData[n * 2 + 0] & 0xFF) | ((rawData[n * 2 + 1] & 0xFF) << 8);
            int end = (n < 3? (((rawData[n * 2 + 2] & 0xFF) | ((rawData[n * 2 + 3] & 0xFF) << 8)) - 5) : rawData.length);

            for (int pos = start; pos < end; pos += 5) {
                Note note = new Note(n);
                // TODO: Decide if byte array is appropriate in Java version.
                byte[] noteData = new byte[5];
                noteData[0] = (byte)(pos + 0 < rawData.length ? rawData[pos + 0] : 0);
                noteData[1] = (byte)(pos + 1 < rawData.length ? rawData[pos + 1] : 0);
                noteData[2] = (byte)(pos + 2 < rawData.length ? rawData[pos + 2] : 0);
                noteData[3] = (byte)(pos + 3 < rawData.length ? rawData[pos + 3] : 0);
                noteData[4] = (byte)(pos + 4 < rawData.length ? rawData[pos + 4] : 0);
                if (note.decode(noteData)) {
                    this.notes.get(n).add(note);
                }
            }
        }
    }
    
    public static class Note {
        
        public int voiceNum;
        public int duration;
        public double frequency;
        public int volume;
        public int origVolume;
        public int frequencyCount;
        public byte[] rawData = null;

        public Note(int voiceNum) {
            this.voiceNum = voiceNum;
        }

        public boolean decode(byte[] rawData) {
            int duration = ((rawData[0] & 0xFF) | ((rawData[1] & 0xFF) << 8));
            if (duration == 0xFFFF) {
                // Two 0xFF bytes in a row at this point ends the current voice.
                return false;
            }
            else {
                this.duration = duration;
                this.frequencyCount = ((rawData[2] & 0x3F) << 4) + (rawData[3] & 0x0F);
                this.origVolume = rawData[4] & 0x0F;
                this.volume = 0x8;  // Volume is set to 0 for PC version, so let's go with 8.
                this.frequency = (frequencyCount > 0 ? 111860.0 / (double)frequencyCount : 0);
                this.rawData = rawData;
                return true;
            }
        }

        public byte[] encode() {
            byte[] rawData = new byte[5];
            int freqdiv = (frequency == 0 ? 0 : (int)(111860 / frequency));
            // Note that the order of the first two bytes is switched around from how it is stored in an AGI SOUND.
            rawData[0] = (byte)(duration & 0xFF);
            rawData[1] = (byte)((duration >> 8) & 0xFF);
            rawData[2] = (byte)((freqdiv >> 4) & 0x3F);
            rawData[3] = (byte)(0x80 | ((voiceNum << 5) & 0x60) | (freqdiv & 0x0F));
            rawData[4] = (byte)(0x90 | ((voiceNum << 5) & 0x60) | (volume & 0x0F));
            this.rawData = rawData;
            return rawData;
        }
    }
}
