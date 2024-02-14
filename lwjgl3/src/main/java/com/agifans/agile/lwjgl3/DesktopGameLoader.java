package com.agifans.agile.lwjgl3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    public void fetchGameFiles(String gameUri, Consumer<Map<String, byte[]>> gameFilesConsumer) {
        Map<String, byte[]> gameFileMap = null;
        
        File file = new File(gameUri.startsWith("file")? URI.create(gameUri).getPath() : gameUri);
        
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getPath());
        }

        if (file.getName().toLowerCase().endsWith(".zip")) {
            gameFileMap = fetchFilesFromZip(file.getAbsoluteFile());
            
        } else {
            File folder = null;
            if (file.isDirectory()) {
                folder = file.getAbsoluteFile();
            } else {
                folder = file.getParentFile();
            }
            gameFileMap = fetchFilesFromFolder(folder);
        }
        
        gameFilesConsumer.accept(gameFileMap);
    }
    
    private Map<String, byte[]> fetchFilesFromZip(File zipFile) {
        Map<String, byte[]> gameFileMap = new HashMap<>();
        FileInputStream fis = null;
        ZipInputStream zis = null;
        
        try {
            fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(fis);
            ZipEntry zipEntry = zis.getNextEntry();
            
            while (zipEntry != null) {
                try {
                    if (!zipEntry.isDirectory()) {
                        gameFileMap.put(zipEntry.getName().toLowerCase(), readBytesFromInputStream(zis));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("IO error reading zip entry: " + zipEntry.getName(), e);
                }
                
                zipEntry = zis.getNextEntry();
            }
            
            return gameFileMap;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ZIP file " + zipFile.getName(), e);
            
        } finally {
            if (zis != null) {
                try {
                    zis.closeEntry();
                } catch (Exception e) {
                    // Ignore.
                }
                try {
                    zis.close();
                } catch (Exception e) {
                    //Ignore.
                }
            }
        }
    }
    
    private Map<String, byte[]> fetchFilesFromFolder(File folder) {
        Map<String, byte[]> gameFileMap = new HashMap<>();
        
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
                throw new IllegalArgumentException("IO error reading file: " + gameFile.getPath(), e);
            }
        }
        
        return gameFileMap;
    }
    
    private byte[] readBytesFromFile(File file) throws FileNotFoundException, IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return readBytesFromInputStream(fis);
        }
    }
    
    private byte[] readBytesFromInputStream(InputStream is) throws IOException {
        int numOfBytesReads;
        byte[] data = new byte[256];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while ((numOfBytesReads = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, numOfBytesReads);
        }
        return buffer.toByteArray();
    }
}
