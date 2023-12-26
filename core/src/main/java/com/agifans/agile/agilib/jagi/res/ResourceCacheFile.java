/**
 * ResourceCacheFile.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z.
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.res;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ResourceCacheFile extends ResourceCache {
    public ResourceCacheFile(File file) throws IOException, ResourceException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        loadFS(file);
    }

    private void loadFS(File file) throws IOException, ResourceException {
        try {
            resProvider = new com.agifans.agile.agilib.jagi.res.v2.ResourceProviderV2(file);
        } catch (ResourceException e) {
            resProvider = new com.agifans.agile.agilib.jagi.res.v3.ResourceProviderV3(file);
        }
    }
}
