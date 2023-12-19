/*
 *  EgaUtil.java
 *  Adventure Game Interpreter AWT Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.awt;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;

/**
 * Misc. Utilities for EGA support in Java's AWT.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public abstract class EgaUtils {
    
    /**
     * EGA Colors Red Band
     */
    protected static final byte[] r = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    
    /**
     * EGA Colors Green Band
     */
    protected static final byte[] g = {(byte) 0x00, (byte) 0x00, (byte) 0xaa, (byte) 0xaa, (byte) 0x00, (byte) 0x00, (byte) 0x55, (byte) 0xaa, (byte) 0x55, (byte) 0x55, (byte) 0xff, (byte) 0xff, (byte) 0x55, (byte) 0x55, (byte) 0xff, (byte) 0xff};
    
    /**
     * EGA Colors Blue Band
     */
    protected static final byte[] b = {(byte) 0x00, (byte) 0xaa, (byte) 0x00, (byte) 0xaa, (byte) 0x00, (byte) 0xaa, (byte) 0x00, (byte) 0xaa, (byte) 0x55, (byte) 0xff, (byte) 0x55, (byte) 0xff, (byte) 0x55, (byte) 0xff, (byte) 0x55, (byte) 0xff};
    
    /**
     * EGA Color Model Cache
     */
    protected static IndexColorModel indexModel;
    
    /**
     * Native Color Model Cache
     */
    protected static DirectColorModel nativeModel;

    /**
     * Returns the ColorModel used by EGA Adapters.
     * <p>
     * Used to convert visual resource from EGA Color Model to the
     * Native Color Model.
     */
    public static synchronized IndexColorModel getIndexColorModel() {
        int i;

        if (indexModel == null) {
            indexModel = new IndexColorModel(8, 16, r, g, b);
        }

        return indexModel;
    }

    /**
     * Returns a ColorModel representing the nativiest ColorModel of the
     * current system configuration.
     * <p>
     * In order to reduce the number of ColorModel convertions, each visual
     * resource is converted as soon as possible to this ColorModel.
     */
    public static synchronized DirectColorModel getNativeColorModel() {
        if (nativeModel == null) {
            ColorModel model = Toolkit.getDefaultToolkit().getColorModel();
            DirectColorModel direct;

            if ((model.getTransferType() != DataBuffer.TYPE_INT) ||
                    !(model instanceof DirectColorModel)) {
                model = ColorModel.getRGBdefault();
            }

            if (model.getTransparency() != Transparency.OPAQUE) {
                direct = (DirectColorModel) model;
                model = new DirectColorModel(
                        direct.getColorSpace(),
                        direct.getPixelSize(),
                        direct.getRedMask(),
                        direct.getGreenMask(),
                        direct.getBlueMask(),
                        0,
                        false,
                        DataBuffer.TYPE_INT);
            }

            nativeModel = (DirectColorModel) model;
        }

        return nativeModel;
    }
}