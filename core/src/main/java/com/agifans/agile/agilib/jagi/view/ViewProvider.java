/*
 *  ViewProvider.java
 *  Adventure Game Interpreter View Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.view;

import java.io.IOException;
import java.io.InputStream;

public interface ViewProvider {
    View loadView(InputStream inputStream, int size) throws IOException, ViewException;
}
