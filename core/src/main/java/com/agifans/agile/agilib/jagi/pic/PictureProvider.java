/*
 *  PictureProvider.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.pic;

import java.io.IOException;
import java.io.InputStream;

public interface PictureProvider {
    Picture loadPicture(InputStream inputStream) throws IOException, PictureException;
}
