/*
 *  PictureEntryChangePicColor.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.pic;

public class PictureEntryChangePicColor extends PictureEntry {
    protected Byte picColor;

    public PictureEntryChangePicColor(Byte picColor) {
        this.picColor = picColor;
    }

    public void draw(PictureContext pictureContext) {
        pictureContext.picColor = pictureContext.translatePixel(picColor);
    }
}
