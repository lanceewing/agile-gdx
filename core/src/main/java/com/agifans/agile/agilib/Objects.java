package com.agifans.agile.agilib;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sierra.agi.inv.InventoryObject;
import com.sierra.agi.inv.InventoryObjects;

public class Objects extends Resource {

    public List<Object> objects;

    public int count() { return objects.size(); }

    public int numOfAnimatedObjects;
    
    public Objects(InventoryObjects jagiObjects) {
        numOfAnimatedObjects = jagiObjects.getNumOfAnimatedObjects();
        objects = new ArrayList<>();
        
        for (InventoryObject jagiObject : jagiObjects.getObjects()) {
            objects.add(new Object(jagiObject.getName(), jagiObject.getLocation()));
        }    
    }
    
    public Objects(Objects objects) {
        this.numOfAnimatedObjects = objects.numOfAnimatedObjects;
        this.objects = new ArrayList<Object>();
        for (Object obj : objects.objects) {
            this.objects.add(new Object(obj.name, obj.room));
        }
    }
    
    public byte[] encode() {
        //MemoryStream stream = new MemoryStream();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // The first two bytes point the start of the object names.
        int numOfObjects = this.objects.size();
        int startOfNames = numOfObjects * 3;
        stream.write(startOfNames & 0xFF);
        stream.write((startOfNames >> 8) & 0xFF);

        // Number of animated objects appears next.
        stream.write(this.numOfAnimatedObjects);

        // Write out the name offsets and room numbers.
        Map<String, Integer> nameToOffsetMap = new HashMap<>();
        List<String> distinctNames = new ArrayList<>();
        int nextNameOffset = startOfNames;
        for (int i=0; i < numOfObjects; i++)
        {
            Object o = this.objects.get(i);
            int nameOffset = nextNameOffset;
            if (nameToOffsetMap.containsKey(o.name))
            {
                // Reuse existing name offset if the name matches one we've already seen.
                nameOffset = nameToOffsetMap.get(o.name);
            }
            else
            {
                // Otherwise use a new name slot.
                nameToOffsetMap.put(o.name, nameOffset);
                distinctNames.add(o.name);
                nextNameOffset += (o.name.length() + 1);
            }
            stream.write(nameOffset & 0xFF);
            stream.write((nameOffset >> 8) & 0xFF);
            stream.write(o.room);
        }

        // Write out the distinct names.
        for (String name : distinctNames)
        {
            for (byte b : name.getBytes(Charset.forName("Cp437")))
            {
                stream.write(b);
            }
            stream.write(0);
        }

        byte[] rawData = stream.toByteArray();

        // Encrypt the raw data if required.
        // TODO: Don't think this is required. SavedGames handles crypt.
        //if (crypted)
        //{
        //    crypt(rawData, 0, rawData.length);
        //}

        return rawData;
    }

    public static class Object {
        
        /**
         * The name of the object.
         */
        public String name;

        /**
         * The room in which the object first appears in the game.
         */
        public int room;

        public Object(String name, int room) {
            this.name = name;
            this.room = room;
        }
    }
}
