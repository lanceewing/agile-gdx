package com.agifans.agile.lwjgl3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.GameLoader;
import com.agifans.agile.PixelData;

/**
 * Desktop implementation of the GameLoader.
 */
public class DesktopGameLoader extends GameLoader {

    /**
     * Constructor for DesktopGameLoader.
     * 
     * @param pixelData
     */
    public DesktopGameLoader(PixelData pixelData) {
        super(pixelData);
    }

    @Override
    public Map<String, byte[]> fetchGameFiles(String gameUri) {
        Map<String, byte[]> gameFileMap = new HashMap<>();
        
        // TODO: Currently we only support file:/// URIs.
        URI uri = URI.create(gameUri);
        File file = new File(uri.getPath());
        
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getPath());
        }

        File folder = null;
        if (file.isDirectory()) {
            folder = file.getAbsoluteFile();
        } else {
            folder = file.getParentFile();
        }
        
        File[] gameFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return isGameFile(name);
            } 
        });
        
        for (File gameFile : gameFiles) {
            try {
                gameFileMap.put(gameFile.getName().toLowerCase(), readBytesFromFile(gameFile));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("File not found: " + gameFile.getPath(), e);
            } catch (IOException e) {
                throw new IllegalArgumentException("IO error reading file: " + file.getPath(), e);
            }
        }
        
        return gameFileMap;
    }
    
    private byte[] readBytesFromFile(File file) throws FileNotFoundException, IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            int numOfBytesReads;
            byte[] data = new byte[256];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while ((numOfBytesReads = fis.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, numOfBytesReads);
            }
            return buffer.toByteArray();
        }
    }
}
