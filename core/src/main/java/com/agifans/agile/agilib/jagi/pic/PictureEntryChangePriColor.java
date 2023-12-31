/*
 *  PictureEntryChangePicColor.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.pic;

public class PictureEntryChangePriColor extends PictureEntry {
    protected Byte priColor;

    public PictureEntryChangePriColor(Byte priColor) {
        this.priColor = priColor;
    }

    public void draw(PictureContext pictureContext) {
        pictureContext.priColor = priColor;
    }
}
