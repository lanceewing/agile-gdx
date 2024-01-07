package com.agifans.agile.gwt;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.util.StringUtils;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

/**
 * Encodes the game file Map in a way that can be instantly sent to the web worker,
 * i.e. in an ArrayBuffer, which is transferable.
 */
public class GameFileMapEncoder {

    private static final int FILE_NAME_BYTES = 12;
    private static final int FILE_LEN_BYTES = 4;
    
    /**
     * Constructor for GameFileMapEncoder.
     */
    public GameFileMapEncoder() {
    }
    
    public ArrayBuffer encodeGameFileMap(Map<String, byte[]> gameFileMap) {
        int totalDataLength = 0;
        for (byte[] fileData : gameFileMap.values()) {
            totalDataLength += (fileData.length + FILE_NAME_BYTES + FILE_LEN_BYTES);
        }
        
        ArrayBuffer arrayBuffer = TypedArrays.createArrayBuffer(totalDataLength);
        Uint8Array array = TypedArrays.createUint8Array(arrayBuffer);
        
        int index = 0;
        
        for (String fileName : gameFileMap.keySet()) {
            // Write out 12 bytes for the name of the file.
            String fileNamePadded = StringUtils.padRightSpaces(fileName, 12);
            byte[] fileNameBytes = fileNamePadded.getBytes(StandardCharsets.ISO_8859_1);
            for (byte b : fileNameBytes) {
                array.set(index++, b);
            }
            
            byte[] fileDataBytes = gameFileMap.get(fileName);
            
            // Write out 4 bytes for the length of the file.
            int fileLength = fileDataBytes.length;
            array.set(index++, ((fileLength >> 0)  & 0xFF));
            array.set(index++, ((fileLength >> 8)  & 0xFF));
            array.set(index++, ((fileLength >> 16) & 0xFF));
            array.set(index++, ((fileLength >> 24) & 0xFF));
            
            // Write out the data in the file.
            for (byte b : fileDataBytes) {
                array.set(index++, b);
            }
        }
        
        return arrayBuffer;
    }
    
    public Map<String, byte[]> decodeGameFileMap(ArrayBuffer arrayBuffer) {
        Map<String, byte[]> gameFileMap = new HashMap<>();
        
        Uint8Array array = TypedArrays.createUint8Array(arrayBuffer);
        
        int totalDataLength = arrayBuffer.byteLength();
        int index = 0;
        
        while (index < totalDataLength) {
            // Read the file name bytes.
            byte[] fileNameBytes = new byte[FILE_NAME_BYTES];
            for (int i=0; i<FILE_NAME_BYTES; i++) {
                fileNameBytes[i] = (byte)(array.get(index++) & 0xFF);
            }
            String fileName = new String(fileNameBytes, StandardCharsets.ISO_8859_1);
            
            // Read the file length bytes.
            int fileLength = (
                    ((array.get(index++) << 0)  & 0x000000FF) |
                    ((array.get(index++) << 8)  & 0x0000FF00) | 
                    ((array.get(index++) << 16) & 0x00FF0000) | 
                    ((array.get(index++) << 24) & 0xFF000000));
            
            // Read in the data for the file.
            byte[] fileDataBytes = new byte[fileLength];
            for (int i=0; i < fileLength; i++) {
                fileDataBytes[i] = (byte)(array.get(index++) & 0xFF);
            }
            
            gameFileMap.put(fileName.trim(), fileDataBytes);
        }
        
        return gameFileMap;
    }
}
