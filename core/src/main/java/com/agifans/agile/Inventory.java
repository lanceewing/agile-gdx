package com.agifans.agile;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Keys;

/**
 * The Inventory class handles the viewing of the player's inventory items.
 */
public class Inventory {

    /**
     * The GameState class holds all of the data and state for the Game currently 
     * being run by the interpreter.
     */
    private GameState state;

    /**
     * Holds the data and state for the user input, i.e. keyboard and mouse input.
     */
    private UserInput userInput;

    /**
     * Provides methods for drawing text on to the AGI screen.
     */
    private TextGraphics textGraphics;

    /**
     * The pixels data for the AGI screen, in which the text will be drawn.
     */
    private PixelData pixelData;

    /**
     * Constructor for Inventory.
     *
     * @param state Holds all of the data and state for the Game currently running.
     * @param userInput Holds the data and state for the user input, i.e. keyboard and mouse input.
     * @param textGraphics Provides methods for drawing text on to the AGI screen.
     * @param pixelData The pixel data for the AGI screen, in which the text will be drawn.
     */
    public Inventory(GameState state, UserInput userInput, TextGraphics textGraphics, PixelData pixelData) {
        this.state = state;
        this.userInput = userInput;
        this.textGraphics = textGraphics;
        this.pixelData = pixelData;
    }

    /**
     * Used during the drawing of the inventory screen to represent a single inventory
     * item name displayed in a specified cell of the two column inventory table.
     */
    class InvItem {
        public byte num;
        public String name;
        public int row;
        public int col;
    }

    /**
     * Shows the inventory screen. Implements the AGI "status" command.
     */
    public void showInventoryScreen() {
        List<InvItem> invItems = new ArrayList<InvItem>();
        byte selectedItemIndex = 0;
        int howMany = 0;
        int row = 2;

        // Switch to the text screen.
        textGraphics.textScreen(15);

        // Construct the table of objects being carried, deciding where on
        // the screen they are to be printed as we go.
        for (byte i=0; i < state.objects.objects.size(); i++) {
            com.agifans.agile.agilib.Objects.Object obj = state.objects.objects.get(i);
            if (obj.room == Defines.CARRYING) {
                InvItem invItem = new InvItem();
                invItem.num = i;
                invItem.name = obj.name;
                invItem.row = row;

                if ((howMany & 1) == 0) {
                    invItem.col = 1;
                }
                else {
                    row++;
                    invItem.col = 39 - invItem.name.length();
                }

                if (i == state.getVar(Defines.SELECTED_OBJ)) selectedItemIndex = (byte)invItems.size();

                invItems.add(invItem);
                howMany++;
            }
        }

        // If no objects in inventory, then say so.
        if (howMany == 0) {
            InvItem invItem = new InvItem();
            invItem.num = 0;
            invItem.name = "nothing";
            invItem.row = row;
            invItem.col = 16;
            invItems.add(invItem);
        }

        // Display the inventory items.
        drawInventoryItems(invItems, invItems.get(selectedItemIndex));

        // If we are not allowing an item to be selected, we simply wait for a key press then return.
        if (!state.getFlag(Defines.ENABLE_SELECT)) {
            userInput.waitForKey();
        }
        else {
            // Otherwise we handle movement between the items and selection of an item.
            while (true) {
                int key = userInput.waitForKey();
                if (key == (UserInput.ASCII | Character.ENTER)) {
                    state.setVar(Defines.SELECTED_OBJ, invItems.get(selectedItemIndex).num);
                    break;
                }
                else if (key == (UserInput.ASCII | Character.ESC)) {
                    state.setVar(Defines.SELECTED_OBJ, 0xFF);
                    break;
                }
                else if ((key == Keys.UP) || (key == Keys.DOWN) || (key == Keys.RIGHT) || (key == Keys.LEFT)) {
                    selectedItemIndex = moveSelect(invItems, key, selectedItemIndex);
                }
            }
        }

        // Switch back to the graphics screen.
        textGraphics.graphicsScreen();
    }

    /**
     * Shows a special view of an object that has an attached description. Intended for use
     * with the "look at object" scenario when the object looked at is an inventory item.
     *
     * @param viewNumber The number of the view to show the special inventory object view of.
     */
    public void showInventoryObject(int viewNumber) {
        // Set up the AnimatedObject that will be used to display this view.
        AnimatedObject aniObj = new AnimatedObject(state, -1);
        aniObj.setView(viewNumber);
        aniObj.x = aniObj.prevX = (short)((Defines.MAXX - aniObj.xSize()) / 2);
        aniObj.y = aniObj.prevY = Defines.MAXY;
        aniObj.priority = 15;
        aniObj.fixedPriority = true;
        aniObj.previousCel = aniObj.cel();

        // Display the description in a window along with the item picture.
        textGraphics.windowPrint(state.views[viewNumber].description, aniObj);

        // Restore the pixels that were behind the item's image.
        aniObj.restoreBackPixels();
        aniObj.show(pixelData);
    }

    /**
     * Draws the table of inventory items.
     *
     * @param invItems The List of the items in the inventory table.
     * @param selectedItem The currently selected item.
     */
    private void drawInventoryItems(List<InvItem> invItems, InvItem selectedItem) {
        textGraphics.drawString(this.pixelData, "You are carrying:", 11 * 8, 0 * 8, 0, 15);

        for (InvItem invItem : invItems) {
            if ((invItem == selectedItem) && state.getFlag(Defines.ENABLE_SELECT)) {
                textGraphics.drawString(this.pixelData, invItem.name, invItem.col * 8, invItem.row * 8, 15, 0);
            }
            else {
                textGraphics.drawString(this.pixelData, invItem.name, invItem.col * 8, invItem.row * 8, 0, 15);
            }
        }

        if (state.getFlag(Defines.ENABLE_SELECT)) {
            textGraphics.drawString(this.pixelData, "Press ENTER to select, ESC to cancel", 2 * 8, 24 * 8, 0, 15);
        }
        else {
            textGraphics.drawString(this.pixelData, "Press a key to return to the game", 4 * 8, 24 * 8, 0, 15);
        }
    }

    /**
     * Processes the direction key that has been pressed. If within the bounds of the
     * inventory List, a new selected item index will be returned and a new inventory
     * item highlighted on the screen.
     *
     * @param invItems 
     * @param dirKey 
     * @param oldSelectedItemIndex 
     * 
     * @return The index of the new selected inventory item.
     */
    private byte moveSelect(List<InvItem> invItems, int dirKey, byte oldSelectedItemIndex) {
        byte newSelectedItemIndex = oldSelectedItemIndex;

        switch (dirKey) {
            case Keys.UP:
                newSelectedItemIndex -= 2;
                break;
            case Keys.RIGHT:
                newSelectedItemIndex += 1;
                break;
            case Keys.DOWN:
                newSelectedItemIndex += 2;
                break;
            case Keys.LEFT:
                newSelectedItemIndex -= 1;
                break;
        }

        if ((newSelectedItemIndex < 0) || (newSelectedItemIndex >= invItems.size())) {
            newSelectedItemIndex = oldSelectedItemIndex;
        }
        else {
            InvItem previousItem = invItems.get(oldSelectedItemIndex);
            InvItem newItem = invItems.get(newSelectedItemIndex);
            textGraphics.drawString(this.pixelData, previousItem.name, previousItem.col * 8, previousItem.row * 8, 0, 15);
            textGraphics.drawString(this.pixelData, newItem.name, newItem.col * 8, newItem.row * 8, 15, 0);
        }

        return newSelectedItemIndex;
    }
}
