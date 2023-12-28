package com.agifans.agile.agilib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.agifans.agile.agilib.jagi.view.View;
import com.agifans.agile.agilib.jagi.view.ViewException;
import com.agifans.agile.agilib.jagi.view.ViewProvider;

/**
 * An implementation of the JAGI ViewProvider interface that loads the View 
 * in a way that doesn't rely on the Java AWT classes, as these are not available
 * with GWT.
 */
public class AgileViewProvider implements ViewProvider {

    @Override
    public View loadView(InputStream is, int size) throws IOException, ViewException {
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
        return new AgileViewWrapper(new com.agifans.agile.agilib.View(buffer.toByteArray()));
    }

    public static class AgileViewWrapper implements View {
        
        private com.agifans.agile.agilib.View agileView;
        
        public AgileViewWrapper(com.agifans.agile.agilib.View agileView) {
            this.agileView = agileView;
        }
        
        public com.agifans.agile.agilib.View getAgileView() {
            return agileView;
        }
    }
}
