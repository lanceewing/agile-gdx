package com.agifans.agile.editor.picture.picedit.gui;

import java.util.LinkedList;

import com.agifans.agile.EgaPalette;
import com.agifans.agile.agilib.picedit.BrushType;
import com.agifans.agile.agilib.picedit.PictureCode;
import com.agifans.agile.agilib.picedit.PictureCodeType;
import com.agifans.agile.editor.picture.picedit.picture.Picture;
import com.agifans.agile.editor.picture.picedit.picture.PictureChangeListener;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * The CellList that holds the human readable list of picture codes for the currently 
 * selected Picture.
 * 
 * @author Lance Ewing
 */
public class PictureCodeList extends CellList<PictureCode> implements PictureChangeListener {

    /**
     * The Picture whose picture codes will be displayed in this JList.
     */
    private Picture picture;
    
    /**
     * Is true if the picture codes are being added to or removed from; otherwise false.
     */
    private boolean pictureCodesAreAdjusting;
    
    // TODO: Implement popup menu.
    ///**
    // * The popup menu to display when someone right clicks on a PictureCodeList item.
    // */
    //private PictureCodeListPopupMenu popupMenu;
    
    /**
     * Handles the rendering of a single item within the PictureCodeList.
     */
    static class PictureCodeCell extends AbstractCell<PictureCode> {

        Picture picture;
        
        PictureCodeCell(Picture picture) {
            this.picture = picture;
        }
        
        @Override
        public void render(Context context, PictureCode value, SafeHtmlBuilder sb) {
            // Value can be null, so do a null check..
            if (value == null) {
              return;
            }
            
            int index = context.getIndex();
            String displayText = null;
            
            if (index == 0) {
                displayText = "Start";
                
            } else {
            
                LinkedList<PictureCode> pictureCodes = picture.getPictureCodes();
                PictureCode pictureCode = pictureCodes.get(index - 1);
                PictureCode previousPictureCode = null;
            
                if (pictureCode.isActionCode()) {
                    PictureCodeType actionCodeType = pictureCode.getType();
                    StringBuilder displayTextBuf = new StringBuilder("  ");
                    displayTextBuf.append(actionCodeType.getDisplayableText());
                    displayText = displayTextBuf.toString();
                } else {
                    StringBuilder displayTextBuf = null;
                    int code = pictureCode.getCode();
                    switch (pictureCode.getType()) {
                        case FILL_POINT_DATA:
                            displayTextBuf = new StringBuilder("    Fill ");
                            displayTextBuf.append((code & 0xFF00) >> 8);
                            displayTextBuf.append(" ");
                            displayTextBuf.append(code & 0x00FF);
                            displayText = displayTextBuf.toString();
                            break;
                        case BRUSH_POINT_DATA:
                            displayTextBuf = new StringBuilder("    Plot ");
                            displayTextBuf.append((code & 0xFF00) >> 8);
                            displayTextBuf.append(" ");
                            displayTextBuf.append(code & 0x00FF);
                            displayText = displayTextBuf.toString();
                            break;
                        case ABSOLUTE_POINT_DATA:
                            previousPictureCode = pictureCodes.get(index  - 2);
                            if (previousPictureCode.isActionCode()) {
                                displayTextBuf = new StringBuilder("    MoveTo ");
                            } else {
                                displayTextBuf = new StringBuilder("    LineTo ");
                            }
                            displayTextBuf.append((code & 0xFF00) >> 8);
                            displayTextBuf.append(" ");
                            displayTextBuf.append(code & 0x00FF);
                            displayText = displayTextBuf.toString();
                            break;
                        case RELATIVE_POINT_DATA:
                            int dx = ((code & 0xF0) >> 4) & 0x0F;
                            int dy = (code & 0x0F);
                            if ((dx & 0x08) > 0) {
                                dx = (-1) * (dx & 0x07);
                            }
                            if ((dy & 0x08) > 0) {
                                dy = (-1) * (dy & 0x07);
                            }
                            StringBuilder displayTextBuilder = new StringBuilder();
                            displayTextBuilder.append("    LineTo ");
                            if (dx >= 0) {
                              displayTextBuilder.append("+");
                            }
                            displayTextBuilder.append(dx);
                            displayTextBuilder.append(" ");
                            if (dy >= 0) {
                              displayTextBuilder.append("+");
                            }
                            displayTextBuilder.append(dy);
                            displayText = displayTextBuilder.toString();
                            break;
                        case X_POSITION_DATA:
                            displayTextBuf = new StringBuilder("    LineTo ");
                            displayTextBuf.append(code);
                            displayTextBuf.append(" +0");
                            displayText = displayTextBuf.toString();
                            break;
                        case Y_POSITION_DATA:
                            displayTextBuf = new StringBuilder("    LineTo +0 ");
                            displayTextBuf.append(code);
                            displayText = displayTextBuf.toString();
                            break;
                        case BRUSH_PATTERN_DATA:
                            displayTextBuf = new StringBuilder("    SetPattern ");
                            displayTextBuf.append(code);
                            displayText = displayTextBuf.toString();
                            break;
                        case BRUSH_TYPE_DATA:
                            displayTextBuf = new StringBuilder("    ");
                            displayTextBuf.append(BrushType.getBrushTypeForBrushCode(pictureCode.getCode()).getDisplayName());
                            displayText = displayTextBuf.toString();
                            break;
                        case COLOR_DATA:
                            displayTextBuf = new StringBuilder("    ");
                            displayTextBuf.append(EgaPalette.COLOR_NAMES[pictureCode.getCode()]);
                            displayText = displayTextBuf.toString();
                            break;
                        case END:
                            displayText = "End";
                            break;
                    }
                }
            }

            // TODO: Handle indenting.
            // TODO: Handle different colour.
            sb.appendHtmlConstant("<table>");
            sb.appendHtmlConstant("<tr><td>");
            sb.appendHtmlConstant(displayText);
            sb.appendHtmlConstant("</td></tr></table>");
        }
    }
    
    /**
     * Constructor for PictureCodeList.
     * 
     * @param picture The Picture whose picture codes will be displayed in this JList.
     */
    public PictureCodeList(Picture picture) {
        super(new PictureCodeCell(picture));
        
        // Holds the picture codes that we are going to render with this list.
        this.picture = picture;

        setWidth("130px");
        
        // Set font to match what the Java PICEDIT uses.
        getElement().getStyle().setProperty("font-family", "monospace");
        getElement().getStyle().setColor("black");
        getElement().getStyle().setFontWeight(FontWeight.BOLD);
        getElement().getStyle().setFontSize(10, Unit.PX);
        
        // Add a selection model so we can select cells.
        final SingleSelectionModel<PictureCode> selectionModel = new SingleSelectionModel<PictureCode>();
        setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                
                // TODO: Use selected object to set the new position.
                // TODO: selectionModel.getSelectedObject()
                
            }
        });
        
        // TODO: Set up the popup menu.
        //popupMenu = new PictureCodeListPopupMenu();
        //this.addMouseListener(new PictureCodeListMouseListener());
    }
    
    // TODO: Implement pop up menu
//    /**
//     * Popup menu that appears over the picture code list when the right mouse button is clicked.
//     */
//    class PictureCodeListPopupMenu extends JPopupMenu {
//        
//        private JMenuItem deleteMenuItem;
//        
//        /**
//         * Constructor for PictureCodeListPopupMenu.
//         */
//        PictureCodeListPopupMenu() {
//            deleteMenuItem = new JMenuItem("Delete");
//            deleteMenuItem.addActionListener(new PictureCodeListPopupMenuActionListener());
//            add(deleteMenuItem);
//        }
//        
//        /**
//         * Refreshes the state of the menu items.
//         */
//        public void refreshState() {
//            boolean deleteEnabledStatus = !picture.getCurrentPictureCode().getType().equals(PictureCodeType.COLOR_DATA);
//            // TODO: Add more checks for delete enabled status, e.g. cannot delete if it would create a corrupt picture.
//            deleteMenuItem.setEnabled(deleteEnabledStatus);
//        }
//    }
    
    /**
     * Completely refreshes the JList content.
     */
    public void refreshList() {
        redraw();
    }
    
    /**
     * Invoked when picture codes are added to the Picture. Delegates to the PictureCodeListModel.
     */
    public void pictureCodesAdded(int fromIndex, int toIndex) {
        pictureCodesAreAdjusting = true;
        redraw();
        pictureCodesAreAdjusting = false;
    }

    /**
     * Invoked when picture codes are removed from the Picture. Delegates to the PictureCodeListModel.
     */
    public void pictureCodesRemoved(int fromIndex, int toIndex) {
        pictureCodesAreAdjusting = true;
        redraw();
        pictureCodesAreAdjusting = false;
    }

    /**
     * Invoked when the picture has forced a collapse of the selection interval.
     */
    public void selectionIntervalCollapsed() {
        // TODO: Not sure what to do yet.
    }
    
    /**
     * Invoked when the position slider value changes. Keeps the picture code list selected index in sync.
     */
    public void stateChanged() {
        // TODO: Not sure what to do yet.
        //int pictureIndex = picture.getPicturePosition() + 1;
        //if (pictureIndex != getMaxSelectionIndex()) {
        //    setSelectedIndex(pictureIndex);
        //}
    }

//    /**
//     * Invoked when the user selects something on the picture code CellList.
//     */
//    public void valueChanged(ListSelectionEvent e) {
//        if (!e.getValueIsAdjusting() && !pictureCodesAreAdjusting) {
//            int selectedIndex = this.getMinSelectionIndex();
//            if (selectedIndex > 0) {
//                int selectedPicturePosition = selectedIndex - 1;
//                
//                // If an action code is selected in isolation then auto-select the associated data codes.
//                List<PictureCode> pictureCodes = picture.getPictureCodes();
//                PictureCode pictureCode = pictureCodes.get(selectedPicturePosition);
//                if (pictureCode.isActionCode() && (getMinSelectionIndex() == getMaxSelectionIndex())) {
//                    // Find the end of the data codes.
//                    int position = selectedPicturePosition + 1;
//                    do {
//                        pictureCode = pictureCodes.get(position++);
//                    } while ((pictureCode != null) && pictureCode.isDataCode());
//                    
//                    // Only if there is at least one data code do we auto-select them.
//                    int dataCodeCount = ((position - selectedIndex) - 1); 
//                    if (dataCodeCount > 0) {
//                        setSelectionInterval(selectedIndex, position - 1);
//                        return;
//                    }
//                }
//                
//                // This check is so that we don't redraw picture if picture is already at the position.
//                if (selectedPicturePosition != picture.getPicturePosition()) {
//                    picture.setPicturePosition(selectedPicturePosition);
//                    picture.drawPicture();
//                }
//                
//                // Auto-scroll the JList to show the selected picture code if it isn't visible.
//                int minSelectionIndex = getMinSelectionIndex();
//                if (minSelectionIndex < getFirstVisibleIndex() || minSelectionIndex > getLastVisibleIndex()) {
//                    int numOfVisibleItems = (getLastVisibleIndex() - getFirstVisibleIndex()) - 1;
//                    int topIndex = minSelectionIndex;
//                    int bottomIndex = Math.min(minSelectionIndex + numOfVisibleItems, picture.getPictureCodes().size() + 1);
//                    scrollRectToVisible(getCellBounds(topIndex, bottomIndex));
//                }
//                
//            } else {
//                // This takes care of moving the selection off the Start item. We don't want that
//                // to be selectable.
//                setSelectedIndex(1);
//                return;
//            }
//        }
//        
//        // Keeps Picture in sync with currently selected items.
//        picture.setSelectionInterval(getMinSelectionIndex() - 1, getMaxSelectionIndex() - 1);
//    }

    
    // TODO: Handle right clicks for popup menu.
//    /**
//     * Mouse Listener for the PictureCodeList CList. Handles the popup menu for items in the list.
//     */
//    class PictureCodeListMouseListener extends MouseAdapter {
//      
//        /**
//         * Invoked when a mouse pressed event occurs on the PictureCodeList JList.
//         * 
//         * @param e The MouseEvent.
//         */
//        public void mousePressed(MouseEvent e) {
//            checkPopup(e);
//        }
//  
//        /**
//         * Invoked when a mouse released event occurs on the PictureCodeList JList.
//         * 
//         * @param e The MouseEvent.
//         */
//        public void mouseReleased(MouseEvent e) {
//            checkPopup(e);
//        }
//        
//        /**
//         * Checks if the mouse event is the popup event. This is the platform 
//         * independent way of implementing this.
//         *  
//         * @param e The MouseEvent to check.
//         */
//        private void checkPopup(MouseEvent e) {
//            if (e.isPopupTrigger()) {
//                // Convert the mouse event in to the corresponding picture position.
//                int itemIndex = locationToIndex(e.getPoint());
//                
//                // Only show the popup menu if the item is selected and it isn't the end code.
//                if ((itemIndex >= getMinSelectionIndex()) && (itemIndex <= getMaxSelectionIndex()) && !picture.getCurrentPictureCode().isEndCode()) {
//                    popupMenu.refreshState();
//                    popupMenu.show(PictureCodeList.this, e.getX(), e.getY());
//                }
//            }
//        }
//    }
//    
//    /**
//     * Enum representing the options in the popup menu.
//     */
//    private enum PopupMenuAction { 
//        DELETE,
//        COPY
//    };
//    
//    /**
//     * ActionListener for the popup menu that is activated for a selected item in the JList.
//     */
//    class PictureCodeListPopupMenuActionListener implements ActionListener {
//
//        /**
//         * Processes the given ActionEvent for the popup menu.
//         * 
//         * @param e The ActionEvent to process.
//         */
//        public void actionPerformed(ActionEvent e) {
//            PopupMenuAction action = PopupMenuAction.valueOf(e.getActionCommand().toUpperCase());
//            switch (action) {
//                case DELETE:
//                    // Store some details about what is being deleted so we can decide what to do after it has been deleted.
//                    int minSelectionIndex = getMinSelectionIndex();
//                    PictureCode codeToDelete = picture.getCurrentPictureCode();
//
//                    // Allow any code other than the end code to be deleted. There must always be an end code.
//                    if (!codeToDelete.isEndCode()) {
//                        // Remember, picture position is always one less than the JList index.
//                        picture.deletePictureCodes(getMinSelectionIndex() - 1, getMaxSelectionIndex() - 1);
//                    }
//                    
//                    // Reselect the current picture position after delete since JList will have lowered index by 1. 
//                    setSelectedIndex(minSelectionIndex);
//                    break;
//            }
//        }
//    }
}
