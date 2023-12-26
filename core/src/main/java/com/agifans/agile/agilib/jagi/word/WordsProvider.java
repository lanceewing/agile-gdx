/*
 *  WordsProvider.java
 *  Adventure Game Interpreter Word Package
 *
 *  Created by Dr. Z
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.word;

import java.io.IOException;
import java.io.InputStream;

public interface WordsProvider {
    Words loadWords(InputStream in) throws IOException;
}
