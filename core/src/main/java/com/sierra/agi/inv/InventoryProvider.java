/**
 * InventoryProvider.java
 * Adventure Game Interpreter Inventory Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.inv;

import java.io.IOException;
import java.io.InputStream;

public interface InventoryProvider {
    InventoryObjects loadInventory(InputStream in) throws IOException;
}
