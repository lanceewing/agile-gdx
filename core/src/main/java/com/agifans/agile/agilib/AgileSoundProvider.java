package com.agifans.agile.agilib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sierra.agi.sound.Sound;
import com.sierra.agi.sound.SoundProvider;

/**
 * An implementation of the JAGI SoundProvider interface that loads the Sound 
 * in a form more easily used by AGILE.
 */
public class AgileSoundProvider implements SoundProvider {

    @Override
    public Sound loadSound(InputStream is) throws IOException {
        // At this point, JAGI has already read the 5 byte header, i.e.
        // 0x12 0x34, etc., which means that the InputStream does not contain
        // the length. We therefore have to fully read the resource from 
        // the InputStream so as to create the byte array required by
        // the AGILE Sound resource. Avoiding Java 9 at present, as it is
        // unclear whether GWT will support this.
        int numOfBytesReads;
        byte[] data = new byte[256];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while ((numOfBytesReads = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, numOfBytesReads);
        }
        buffer.flush();
        return new AgileSoundWrapper(new com.agifans.agile.agilib.Sound(buffer.toByteArray()));
    }

    public static class AgileSoundWrapper implements Sound {
        
        private com.agifans.agile.agilib.Sound agileSound;
        
        public AgileSoundWrapper(com.agifans.agile.agilib.Sound agileSound) {
            this.agileSound = agileSound;
        }
        
        public com.agifans.agile.agilib.Sound getAgileSound() {
            return agileSound;
        }
    }
}
