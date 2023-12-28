package com.agifans.agile.agilib;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.agifans.agile.EgaPalette;

public class View extends Resource {

    public ArrayList<Loop> loops;
    public String description;
    
    public View(byte[] rawData) {
        loops = new ArrayList<>();
        
        decode(rawData);
    }
    
    public void decode(byte[] rawData) {
        int numLoops = (rawData[2] & 0xFF);
        
        // Read the description
        int textPosition = (rawData[3] & 0xFF) + ((rawData[4] & 0xFF) << 8);
        if (textPosition == 0) {
            description = "";
        }
        else {
            // Find the description end
            int textEnd = textPosition;
            while ((textEnd < rawData.length) && (rawData[textEnd] != 0)) {
                textEnd++;
            }
            description = new String(rawData, textPosition, textEnd - textPosition, StandardCharsets.ISO_8859_1);
        }
        
        // Are there any loops?
        if (numLoops == 0) return;
        
        int headerPos = 5;  // Start reading at byte 5 (the first loop)
        
        for (int loopNumber = 0; loopNumber < numLoops; loopNumber++) {
            int loopPosition = (rawData[headerPos] & 0xFF) + ((rawData[headerPos + 1] & 0xFF) << 8);
            loops.add(new Loop(rawData, loopPosition, loopNumber));
            headerPos += 2;
        }
    }
    
    public class Loop {
        
        public ArrayList<Cel> cels;
        
        public Loop(byte[] rawData, int offset, int loopNumber) {
            cels = new ArrayList<>();
            
            int numCels = (rawData[offset] & 0xFF);
            int headerPos = offset + 1;
            
            for (int cellNumber = 0; cellNumber < numCels; cellNumber++) {
                int celPosition = (rawData[headerPos] & 0xFF) + ((rawData[headerPos + 1] & 0xFF) << 8);
                cels.add(new Cel(rawData, offset + celPosition, loopNumber));
                headerPos += 2;
            }
        }
    }
    
    public class Cel {
        
        private short width;

        private short height;
        
        private int[] data;
        
        private int transparent;
        
        public Cel(byte[] rawData, int offset, int loopNumber) {
            width = (short)(rawData[offset] & 0xFF);
            height = (short)(rawData[offset + 1] & 0xFF);
            
            short transMirror = (short)(rawData[offset + 2] & 0xFF);
            short mirrorInfo = (short)((transMirror & 0xF0) >> 4);

            transparent = EgaPalette.colours[(transMirror & 0x0F)];
            
            loadData(rawData, offset + 3);

            if ((mirrorInfo & 0x8) != 0) {
                if ((mirrorInfo & 0x7) != loopNumber) {
                    mirror();
                }
            }
        }
        
        private void loadData(byte[] rawData, int offset) {
            int x;
            
            data = new int[width * height];

            for (int j = 0, y = 0; y < height; y++) {
                for (x = 0; rawData[offset] != 0; offset++) {
                    int colorRGB = EgaPalette.colours[(rawData[offset] & 0xF0) >> 4];
                    int count = (rawData[offset] & 0x0F);
                    for (int i = 0; i < count; i++, j++, x++) {
                        data[j] = colorRGB;
                    }
                }

                for (; x < width; j++, x++) {
                    data[j] = transparent;
                }

                offset++;
            }
        }
        
        private void mirror() {
            for (int y = 0; y < height; y++) {
                for (int x1 = width - 1, x2 = 0; x1 > x2; x1--, x2++) {
                    int i1 = (y * width) + x1;
                    int i2 = (y * width) + x2;
                    int b = data[i1];
                    data[i1] = data[i2];
                    data[i2] = b;
                }
            }
        }
        
        public short getWidth() {
            return width;
        }

        public short getHeight() {
            return height;
        }

        public int[] getPixelData() {
            return data;
        }

        public int getTransparentPixel() {
            return transparent;
        }
    }
}
