package com.agifans.agile;

import java.util.ArrayList;
import java.util.List;

import com.agifans.agile.TextGraphics.TextWindow;
import com.badlogic.gdx.Input.Keys;

/**
 * The Menu class is responsible for processing both the AGI commands that define the 
 * menus and their items and also for rendering the menu system when it is activated and 
 * processing the navigation and selection events while it is open.
 */
public class Menu {

    // Various static constants for calculating menu window dimensions and position.
    private static final int CHARWIDTH = 4;
    private static final int CHARHEIGHT = 8;
    private static final int VMARGIN = 8;
    private static final int HMARGIN = CHARWIDTH;

    /**
     * The GameState class holds all of the data and state for the Game currently 
     * being run by the interpreter.
     */
    private GameState state;

    /**
     * The pixels data for the AGI screen, in which the text will be drawn.
     */
    private PixelData pixelData;

    /**
     * Holds the data and state for the user input, i.e. keyboard and mouse input.
     */
    private UserInput userInput;

    /**
     * Provides methods for drawing text on to the AGI screen.
     */
    private TextGraphics textGraphics;

    /**
     * The List of the top level menu headers currently defined in the menu system.
     */
    private List<MenuHeader> headers;

    /**
     * The currently highlighted item in the currently open menu header.
     */
    private MenuItem currentItem;

    /**
     * The currently open menu header, i.e. the open whose items are currently being displayed.
     */
    private MenuHeader currentHeader;

    private int menuCol;
    private int itemRow;
    private int itemCol;

    /**
     * If set to true then this prevents further menu definition commands from being processed.
     */
    private boolean menuSubmitted;

    class MenuHeader {
        public MenuItem title;
        public List<MenuItem> items;
        public MenuItem currentItem;
        public int height;
    }

    class MenuItem {
        public String name;
        public int row;
        public int col;
        public boolean enabled;
        public int controller;
    }

    /**
     * Constructor for Menu.
     *
     * @param state 
     * @param textGraphics 
     * @param pixelData 
     * @param userInput 
     */
    public Menu(GameState state, TextGraphics textGraphics, PixelData pixelData, UserInput userInput) {
        this.state = state;
        this.textGraphics = textGraphics;
        this.headers = new ArrayList<MenuHeader>();
        this.pixelData = pixelData;
        this.userInput = userInput;
    }

    /**
     * Creates a new menu with the given name.
     *
     * @param menuName The name of the new menu.
     */
    public void setMenu(String menuName) {
        // We can't accept any more menu definitions if submit.menu has already been executed.
        if (menuSubmitted) return;

        if (currentHeader == null) {
            // The first menu header starts at column 1.
            menuCol = 1;
        }
        else if (currentHeader.items.size() == 0) {
            // If the last header didn't have any items, then disable it.
            currentHeader.title.enabled = false;
        }

        // Create a new MenuHeader.
        MenuHeader header = new MenuHeader();

        // Set the position of this menu name in the menu strip (leave two  
        // chars between menu titles).
        header.title = new MenuItem();
        header.title.row = 0;
        header.title.name = menuName;
        header.title.col = menuCol;
        header.title.enabled = true;
        header.items = new ArrayList<MenuItem>();
        header.height = 0;

        this.currentHeader = header;
        this.headers.add(header);

        // Adjust the menu column for the next header.
        menuCol += menuName.length() + 1;

        // Initialize stuff for the menu items to follow.
        currentItem = null;
        itemRow = 1;
    }

    /**
     * Creates a new menu item in the current menu, of the given name and mapped
     * to the given controller number.
     *
     * @param itemName The name of the new menu item.
     * @param controller The number of the controller to map this menu item to.
     */
    public void setMenuItem(String itemName, int controller) {
        // We can't accept any more menu definitions if submit.menu has already been executed.
        if (menuSubmitted) return;

        // Create and define the new menu item and its position.
        MenuItem menuItem = new MenuItem();
        menuItem.name = itemName;
        menuItem.controller = controller;
        if (itemRow == 1) {
            if (currentHeader.title.col + itemName.length() < 39) {
                itemCol = currentHeader.title.col;
            }
            else {
                itemCol = 39 - itemName.length();
            }
        }
        menuItem.row = ++itemRow;
        menuItem.col = itemCol;
        menuItem.enabled = true;

        // Add the menu item to the current header's item list.
        currentItem = menuItem;
        currentHeader.items.add(menuItem);
        currentHeader.height++;
        if (currentHeader.currentItem == null) {
            currentHeader.currentItem = menuItem;
        }
    }

    /**
     * Signals to the menu system that the menu has now been fully defined. No further SetMenu
     * or SetMenuItem calls will be processed. The current header and item is reset back to the
     * first item in the first menu, ready for usage when the menu is activated.
     */
    public void submitMenu() {
        // If the last menu didn't have any items, disable it.
        if (currentHeader.items.size() == 0) {
            currentHeader.title.enabled = false;
        }

        // Make the first menu the current one.
        currentHeader = (headers.size() > 0? headers.get(0) : null);
        currentItem = ((currentHeader != null) && (currentHeader.items.size() > 0) ? currentHeader.items.get(0) : null);

        // Remember that the submit has happened. We can't process menu definitions after submit.menu
        menuSubmitted = true;
    }

    /**
     * Enables all MenuItems that map to the given controller number.
     *
     * @param controller The controller whose menu items should be enabled.
     */
    public void enableItem(int controller) {
        for  (MenuHeader header : headers) {
            for (MenuItem item : header.items) {
                if (item.controller == controller) {
                    item.enabled = true;
                }
            }
        }
    }

    /**
     * Enables all MenuItems.
     */
    public void enableAllMenus() {
        for (MenuHeader header : headers) {
            for (MenuItem item : header.items) {
                item.enabled = true;
            }
        }
    }

    /**
     * Disables all MenuItems that map to the given controller number.
     *
     * @param controller The controller whose menu items should be disabled.
     */
    public void disableItem(int controller) {
        for (MenuHeader header : headers) {
            for (MenuItem item : header.items) {
                if (item.controller == controller) {
                    item.enabled = false;
                }
            }
        }
    }

    /**
     * Opens the menu system and processes all the navigation events until an item is either
     * selected or the ESC key is pressed.
     */
    public void menuInput() {
        // Not sure why there is an ENABLE_MENU flag and the allow.menu command, but there is.
        if (state.getFlag(Defines.ENABLE_MENU) && state.menuEnabled) {
            // Clear the menu bar to white.
            textGraphics.clearLines(0, 0, 15);

            // Draw each of the header titles in deselected mode.
            for (MenuHeader header : headers) deselect(header.title);

            // Starts by showing the currently selected menu header and item.
            showMenu(currentHeader);

            // Now we process all navigation keys until we the user either makes a selection
            // or exits the menu system.
            while (true) {
                int index;

                switch (userInput.waitForKey()) {

                    case (UserInput.ASCII | Character.ENTER):  // Select the currently highlighted menu item.
                        if (!currentItem.enabled) continue;
                        state.controllers[currentItem.controller] = true;
                        putAwayMenu(currentHeader, currentItem);
                        restoreMenuLine();
                        state.menuOpen = false;
                        return;

                    case (UserInput.ASCII | Character.ESC):   // Exit the menu system without a selection.
                        putAwayMenu(currentHeader, currentItem);
                        restoreMenuLine();
                        state.menuOpen = false;
                        return;

                    case Keys.UP:                // Moving up within current menu.
                        deselect(currentItem);
                        index = (currentHeader.items.indexOf(currentItem) + currentHeader.items.size() - 1) % currentHeader.items.size();
                        currentItem = currentHeader.items.get(index);
                        select(currentItem);
                        break;

                    case Keys.PAGE_UP:             // Move to top item of current menu.
                        deselect(currentItem);
                        currentItem = currentHeader.items.get(0);
                        select(currentItem);
                        break;

                    case Keys.RIGHT:              // Move to the menu on the right of the current menu..
                        putAwayMenu(currentHeader, currentItem);
                        index = headers.indexOf(currentHeader);
                        do { currentHeader = headers.get((index = ((index + 1) % headers.size()))); }
                        while (!currentHeader.title.enabled);
                        currentItem = currentHeader.currentItem;
                        showMenu(currentHeader);
                        break;

                    case Keys.PAGE_DOWN:           // Move to bottom item of current menu.
                        deselect(currentItem);
                        currentItem = currentHeader.items.get(headers.size() - 1);
                        select(currentItem);
                        break;

                    case Keys.DOWN:               // Move down within current menu.
                        deselect(currentItem);
                        index = (currentHeader.items.indexOf(currentItem) + 1) % currentHeader.items.size();
                        currentItem = currentHeader.items.get(index);
                        select(currentItem);
                        break;

                    case Keys.END:                // Move to the rightmost menu.
                        putAwayMenu(currentHeader, currentItem);
                        currentHeader = headers.get(headers.size() - 1);
                        currentItem = currentHeader.currentItem;
                        showMenu(currentHeader);
                        break;

                    case Keys.LEFT:               // Move left within current menu.
                        putAwayMenu(currentHeader, currentItem);
                        index = headers.indexOf(currentHeader);
                        do { currentHeader = headers.get((index = ((index + headers.size() - 1) % headers.size()))); }
                        while (!currentHeader.title.enabled);
                        currentItem = currentHeader.currentItem;
                        showMenu(currentHeader);
                        break;

                    case Keys.HOME:               // Move to the leftmost menu.
                        putAwayMenu(currentHeader, currentItem);
                        currentHeader = headers.get(0);
                        currentItem = currentHeader.currentItem;
                        showMenu(currentHeader);
                        break;
                }
            }
        }
    }

    /**
     * Restores the state of what the menu line would have looked like prior to the menu being activated.
     */
    private void restoreMenuLine() {
        if (state.showStatusLine) {
            textGraphics.updateStatusLine();
        }
        else {
            textGraphics.clearLines(0, 0, 0);
        }
    }

    /**
     * Shows the menu items for the given MenuHeader.
     *
     * @param header The MenuHeader to show the menu items of.
     */
    private void showMenu(MenuHeader header) {
        // Interestingly, it would seem that the width is always calculated using the first item. The
        // original AGI games tended to make the item names a consistent length within each menu.
        MenuItem firstItem = (header.items.size() > 0 ? header.items.get(0) : null);
        int height = header.height;
        int width = (firstItem != null ? firstItem.name.length() : header.title.name.length());
        int column = (firstItem != null ? firstItem.col : header.title.col);

        // Compute window size and position and put them into the appropriate bytes of the words.
        int menuDim = ((height * CHARHEIGHT + 2 * VMARGIN) << 8) | (width * CHARWIDTH + 2 * HMARGIN);
        int menuPos = (((column - 1) * CHARWIDTH) << 8) | ((height + 1) * CHARHEIGHT + VMARGIN - 1);

        // Show the menu title as being selected.
        select(header.title);

        // Open a window for this menu using the calculated position and dimensions.
        textGraphics.openWindow(new TextWindow(menuPos, menuDim, 15, 0));

        // Render each of the items in this menu.
        for (MenuItem item : header.items) {
            if (item == header.currentItem) {
                select(item);
            }
            else {
                deselect(item);
            }
        }
    }

    /**
     * Puts away the menu so that it is no longer displayed, but remembers what item
     * in the list was selected at the time it was put away.
     *
     * @param header The MenuHeader representing the menu to put away.
     * @param item The MenuItem that was currently selected in the menu when it was put away.
     */
    private void putAwayMenu(MenuHeader header, MenuItem item) {
        header.currentItem = item;
        deselect(header.title);
        textGraphics.closeWindow();
    }

    /**
     * Renders the given MenuItem in a selected state.
     *
     * @param item The MenuItem to render in the selected state.
     */
    private void select(MenuItem item) {
        textGraphics.drawString(pixelData, item.name, item.col * 8, item.row * 8, 15, 0, !item.enabled);
    }

    /**
     * Renders the given MenuItem in a deselected state.
     *
     * @param item The MenuItem to render in the deselected state.
     */
    private void deselect(MenuItem item) {
        textGraphics.drawString(pixelData, item.name, item.col * 8, item.row * 8, 0, 15, !item.enabled);
    }
}