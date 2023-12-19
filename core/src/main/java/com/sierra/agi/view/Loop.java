/*
 *  Loop.java
 *  Adventure Game Interpreter View Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.view;

import com.sierra.agi.io.ByteCaster;

/**
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class Loop {
    /**
     * Cells
     */
    protected Cel[] cels = null;

    /**
     * Creates new Loop
     */
    public Loop(Cel[] cels) {
        this.cels = cels;
    }

    public Loop(byte[] b, int start, int loopNumber) {
        short cellCount;
        int i, j;

        cellCount = ByteCaster.lohiUnsignedByte(b, start);
        cels = new Cel[cellCount];

        j = start + 1;
        for (i = 0; i < cellCount; i++) {
            cels[i] = new Cel(b, start + ByteCaster.lohiUnsignedShort(b, j), loopNumber);
            j += 2;
        }
    }

    public Cel getCell(int cellNumber) {
        return cels[cellNumber];
    }

    public short getCellCount() {
        return (short) cels.length;
    }
}