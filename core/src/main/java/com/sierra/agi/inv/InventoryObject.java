/*
 * InventoryObject.java
 */

package com.sierra.agi.inv;

/**
 * @author Dr. Z
 * @version 0.00.00.01
 */
public final class InventoryObject {
    /**
     * Name
     */
    public String name;
    /**
     * Location
     */
    private final short location;

    public InventoryObject(short location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public short getLocation() {
        return location;
    }
}