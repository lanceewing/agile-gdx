/*
 *  SoundProvider.java
 *  Adventure Game Interpreter Sound Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.sound;

import java.io.IOException;
import java.io.InputStream;

public interface SoundProvider {
    Sound loadSound(InputStream inputStream) throws IOException;
}
