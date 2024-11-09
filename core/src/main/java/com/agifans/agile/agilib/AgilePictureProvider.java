package com.agifans.agile.agilib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.agifans.agile.agilib.jagi.pic.Picture;
import com.agifans.agile.agilib.jagi.pic.PictureException;
import com.agifans.agile.agilib.jagi.pic.PictureProvider;

public class AgilePictureProvider implements PictureProvider {

    @Override
    public Picture loadPicture(InputStream is) throws IOException, PictureException {
        // At this point, JAGI has already read the 5 byte header, i.e.
        // 0x12 0x34, etc., which means that the InputStream does not contain
        // the length. We therefore have to fully read the resource from 
        // the InputStream so as to create the byte array required by
        // the AGILE Picture resource. Avoiding Java 9 at present, as it is
        // unclear whether GWT will support this.
        
        //int numOfBytesReads;
        //byte[] data = new byte[256];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        //while ((numOfBytesReads = is.read(data, 0, data.length)) != -1) {
        //    buffer.write(data, 0, numOfBytesReads);
        //}
        // TODO: 1 byte at a time. multi byte isn't implemented.
        int b = 0;
        while ((b = is.read()) != -1) {
            buffer.write(b);
        }
        buffer.write(0xFF);
        buffer.flush();
        
        return new AgilePictureWrapper(new com.agifans.agile.agilib.Picture(buffer.toByteArray()));
    }
    
    public static class AgilePictureWrapper implements Picture {
        
        private com.agifans.agile.agilib.Picture agilePicture;
        
        public AgilePictureWrapper(com.agifans.agile.agilib.Picture agilePicture) {
            this.agilePicture = agilePicture;
        }
        
        public com.agifans.agile.agilib.Picture getAgilePicture() {
            return agilePicture;
        }
    }
}
