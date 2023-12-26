/**
 * LogicProvider.java
 * Adventure Game Interpreter Logic Package
 * <p>
 * Created by Dr. Z.
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.logic;

import java.io.IOException;
import java.io.InputStream;

public interface LogicProvider {
    Logic loadLogic(short logicNumber, InputStream inputStream, int size) throws IOException, LogicException;
}
