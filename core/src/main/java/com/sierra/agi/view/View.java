/*
 *  View.java
 *  Adventure Game Interpreter View Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.view;

/**
 * View object.
 * <p>
 * View resources contain some of the graphics for the game. Unlike the picture
 * resources which are full-screen background images, view resources are smaller
 * 'sprites' used in the game, such as animations and objects. They are also
 * stored as bitmaps, whereas pictures are stored in vector format.
 * </P><P>
 * Each view resource consists of one or more 'loops'. Each loop in the resource
 * consists of one or more 'cells' (frames). Thus several animations can be
 * stored in one view, or a view can just be used for a single image. The
 * maximum number of loops supported by the interpreter is 255 (0-254) and the
 * maximum number of cells in each is 255 (0-254).
 * </P><P>
 * <B>View header (7+ bytes)</B><BR>
 * Note: ls,ms means that the value is a two-byte word, with the least
 * significant byte stored first, and the most significant byte stored second,
 * e.g. 12 07 is acually 712 (hex) or 1810 (decimal). Most word values in AGI
 * are stored like this, but not all.
 * </P>
 * <TABLE BORDER=1>
 * <TR><TD>Byte</TD><TD>Meaning</TD></TR>
 * <TR><TD>0</TD><TD>Unknown (always seems to be either 1 or 2)</TD></TR>
 * <TR><TD>1</TD><TD>Unknown (always seems to be 1)</TD></TR>
 * <TR><TD>2</TD><TD>Number of loops</TD></TR>
 * <TR><TD>3-4</TD><TD>Position of description (more on this later) (ls,ms) Both bytes are 0 if there is no description.</TD></TR>
 * <TR><TD>5-6</TD><TD>Position of first loop (ls,ms)</TD></TR>
 * <TR><TD>7-8</TD><TD>Position of second loop (if any) (ls,ms)</TD></TR>
 * <TR><TD>9-10</TD><TD>Position of third loop (if any) (ls,ms)</TD></TR>
 * <TR><TD COLSPAN=2>....</TD></TR>
 * </TABLE>
 * <p>
 * Note: Two of these loop references CAN point to the same place. This is done
 * when you want to use mirroring (more on this later).
 * </P>
 * <TABLE BORDER=1>
 * <TR><TD COLSPAN=2><B>Loop Header (3+ bytes)</B></TD></TR>
 * <TR><TD>Byte</TD><TD>Meaning</TD></TR>
 * <TR><TD>0</TD><TD>Number of cells in this loop</TD></TR>
 * <TR><TD>1-2</TD><TD>Position of first cell, relative to start of loop (ls,ms)</TD></TR>
 * <TR><TD>3-4</TD><TD>Position of second cell (if any), relative to start of loop (ls,ms)</TD></TR>
 * <TR><TD>5-6</TD><TD>Position of third cell (if any), relative to start of loop (ls,ms)</TD></TR>
 * </TABLE>
 * <p>
 * <TABLE BORDER=1>
 * <TR><TD COLSPAN=2><B>Cell Header (3 bytes)</B></TD></TR>
 * <TR><TD>Byte</TD><TD>Meaning</TD></TR>
 * <TR><TD>0</TD><TD>Width of cell (remember that AGI pixels are 2 normal EGA pixels wide so a cel of width 12 is actually 24 pixels wide on screen)</TD></TR>
 * <TR><TD>1</TD><TD>Height of cell</TD></TR>
 * <TR><TD>2</TD><TD>Transparency and cell mirroring</TD></TR>
 * </TABLE>
 * </P><P>
 * The first four bits of this byte tell the interpreter how to handle the
 * mirroring of this cell (explained later). The last four bits represent the
 * transparent color. When the cell is drawn on the screen, any pixels of this
 * color will show through to the background. All cells have a transparent color,
 * so if you want an opaque cell then you must set the transparent color to one
 * that is not used in the cell.
 * </P><P>
 * <B>Cell data</B><BR>
 * The actual image data for each cell is stored using RLE (run length encoding)
 * compression. This means that instead of having one byte for each single pixel
 * (or 1/2 byte as you would use for 16 colors), each byte specifies how many
 * pixels there are to be in a row and what colour they are. I will refer to
 * these groups of pixels as 'chunks'.
 * </P><P>
 * This method of compression is not very efficient if there is a lot of single
 * pixels in the image (e.g. a view showing static on a TV screen), but in most
 * cases it does save a fair amount of space.
 * </P><P>
 * Each line (not to be confused with a chunk) in the cell consists of several
 * bytes of pixel data, then a 0 to end the line. Each byte of pixel data
 * represents one chunk. The first four bits determine the colour, and the last
 * four bits determine the number of pixels in the chunk.
 * <BR>
 * e.g. AX BY CZ 00<BR>
 * <BR>
 * This line will have: X pixels of colour A [AX]<BR>
 * <LI>Y pixels of colour B [BY]</LI>
 * <LI>Z pixels of colour C [CZ]</LI>
 * <LI>(then that will be the end of the line) [00]</LI>
 * <BR><BR>
 * If the color of the last chunk on the line is the transparent color, there is
 * no need to store this. For example, if C was the transparent color in the
 * above example, you could just write AX BY 00. This also saves some space.
 * </P><P>
 * <B>Mirroring</B><BR>
 * Mirroring is when you have one loop where all the cells are a mirror image of
 * the corresponding cells in another loop. Although you could do this manually
 * by drawing one loop and then copying and pasting all the cells to another loop
 * and flipping them horizontally, AGI views provide the ability to have this
 * done automatically - you can draw one loop, and have another loop which is
 * set as a mirror of this loop. Thus, when you change one loop you change the
 * other. This is useful if you have an animation of a character walking left
 * and right - you just draw the right-walking animation and have another loop a
 * mirror of this which will have the left-walking animation. Another advantage
 * of cell mirroring is to save space - it doesn't make much difference these
 * days, but back when AGI was written the games were designed to run on 256k
 * systems which meant that memory had to be used as efficiently as possible.
 * </P><P>
 * Mirroring is done by having both loops share the same cell data - you saw
 * above that you can have two loop references pointing to the same place. The
 * first four bits of the 3rd byte in the header of each cell tell the interpreter
 * what is mirrored:<BR>
 * <LI>Bit 1 specifies whether or not this cell is mirrored.</LI>
 * <LI>Bits 1, 2 and 3 specify the number of the loop (from 0-7) which is NOT mirrored.</LI>
 * </P><P>
 * When the interpreter goes to display a loop, it looks at the bit 1 and sees
 * if it is mirrored or not. If it is, then it checks the loop number - if this
 * is NOT the same as the current loop, then it flips the cell before displaying it.
 * </P><P>
 * Leaving enough room for the mirrored image:<BR>
 * If you have a cell that is mirrored, you need to ensure that the number of
 * bytes the cell takes up in the resource is greater than or equal to the number
 * of bytes that the flipped version of the cell would take up.
 * </P><P>
 * The reason for this is that the interpreter loads the view resource into
 * memory and works with that for displaying cells, rather than decoding it and
 * storing it in memory as a non-compressed bitmap. I assume that it doesn't
 * even bother 'decoding' it as such - it probably just draws the chunks of
 * color on the screen as they are. When it has to display the flipped version
 * of a cell, instead of storing the flipped cell somewhere else in memory, it
 * flips the version that is there. So in memory you have the view resource that
 * was read from the file, except that some of the cells have been changed. This
 * is why there is mirroring information stored in each cell - the interpreter
 * has to know what cells have been changed. When it flips a cell, it changes the
 * loop number in the 3rd byte of the cell header to the number of the loop it is
 * currently displaying the cell for. So when it looks at this number the next
 * time for a different loop, it will see that the cell is not the right way
 * round for that loop and mirror it again.
 * </P><P>
 * This process seems very inefficient to me. I don't know why it doesn't just
 * draw the chunks of color on the screen back to front. But since it does it
 * this way we have to make sure that there is enough room for the flipped cell.
 * </P><P>
 * It seems that not all versions of the interpreter require this, however - I
 * was working with version 2.917 when I was testing this out, but I noticed that
 * versoin 2.440 did not require this. I will attempt to try this with all
 * different interpreters and provide a list of which ones use this in the next
 * version of this document. But it is best to put these bytes in just in case,
 * as the views will still work regardless.
 * </P><P>
 * <B>Description</B><BR>
 * All the Views in the game that are used as close-ups of inventory items have
 * a description. When a player 'examines' these (in some games you can select
 * 'see object' from the menu), they will see the first cell of the first loop of
 * this view and the description of the object they are examining. This is
 * brought up using the show.obj command. The Description is stored in plain
 * text, and terminated by a null character. Lines are separated by an 0x0A.
 * </P>
 *
 * @author Dr. Z
 * @author Lance Ewing (Documentation)
 * @version 0.00.00.01
 */
public class View {
    /**
     * Inventory objects have descriptions.
     */
    protected String description;

    /**
     * Loops
     */
    protected Loop[] loops;

    /**
     * Creates a new View representation.
     *
     * @param context Game's Context (facultative)
     * @param stream  View's Data
     * @param size    View's Size (facultative if the stream.available() returns
     *                the size of the view.)
     * @throws ViewException If error occur when loading while loading,
     *                       ViewException is throwed.
     */
    public View(Loop[] loops, String description) throws ViewException {
        this.loops = loops;
        this.description = description;
    }

    /**
     * Obtain a specific loop.
     *
     * @param loopNumber Loop number.
     * @return returns the wanted loop object.
     */
    public Loop getLoop(short loopNumber) {
        return loops[loopNumber];
    }

    /**
     * Obtain the loop count.
     *
     * @return Returns the loop count.
     */
    public short getLoopCount() {
        return (short) loops.length;
    }

    public String getDescription() {
        return description;
    }
}