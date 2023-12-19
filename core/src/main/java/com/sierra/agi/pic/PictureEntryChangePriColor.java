/*
 *  PictureEntryChangePicColor.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.pic;

public class PictureEntryChangePriColor extends PictureEntry {
    protected byte priColor;

    public PictureEntryChangePriColor(byte priColor) {
        this.priColor = priColor;
    }

    public void draw(PictureContext pictureContext) {
        pictureContext.priColor = priColor;
    }
}
