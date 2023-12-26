package com.agifans.agile.agilib;

import java.util.ArrayList;

public class View extends Resource {

    public ArrayList<Loop> loops;
    public String description;
    
    public View(com.agifans.agile.agilib.jagi.view.View jagiView) {
        description = jagiView.getDescription();
        loops = new ArrayList<>();
        for (short loopNum = 0; loopNum < jagiView.getLoopCount(); loopNum++) {
            loops.add(new Loop(jagiView.getLoop(loopNum)));
        }
    }
    
    public class Loop {
        
        public ArrayList<Cel> cels;
        
        public Loop(com.agifans.agile.agilib.jagi.view.Loop jagiLoop) {
            cels = new ArrayList<>();
            for (short cellNum = 0; cellNum < jagiLoop.getCellCount(); cellNum++) {
                cels.add(new Cel(jagiLoop.getCell(cellNum)));
            }
        }
    }
    
    public class Cel {
        
        private com.agifans.agile.agilib.jagi.view.Cel jagiCel;
        
        public Cel(com.agifans.agile.agilib.jagi.view.Cel jagiCel) {
            this.jagiCel = jagiCel;
        }
        
        public short getWidth() {
            return jagiCel.getWidth();
        }

        public short getHeight() {
            return jagiCel.getHeight();
        }

        public int[] getPixelData() {
            return jagiCel.getPixelData();
        }

        public int getTransparentPixel() {
            return jagiCel.getTransparentPixel();
        }
    }
}
