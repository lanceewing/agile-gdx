/*
 *  PictureEntryChangePen.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.pic;

public class PictureEntryChangePen extends PictureEntry {
    protected byte penStyle;

    public PictureEntryChangePen(byte penStyle) {
        this.penStyle = penStyle;
    }

    public void draw(PictureContext pictureContext) {
        pictureContext.penStyle = penStyle;
    }
}
