/*
 *  ResourceCacheFileDebug.java
 *  Adventure Game Interpreter Logic Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.res;

import java.io.File;
import java.io.IOException;

public class ResourceCacheFileDebug extends ResourceCacheFile {
    public ResourceCacheFileDebug(File file) throws IOException, ResourceException {
        super(file);
    }
}