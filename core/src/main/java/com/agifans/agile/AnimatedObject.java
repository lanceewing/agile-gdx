package com.agifans.agile;

import com.agifans.agile.agilib.Picture;
import com.agifans.agile.agilib.View;
import com.agifans.agile.agilib.View.Cel;
import com.agifans.agile.agilib.View.Loop;
import com.agifans.agile.util.StringUtils;

/**
 * The AnimatedObject class is one of the core classes in the AGI interpreter. An instance of
 * this class holds the state of an animated object on the screen. Many of the action commands
 * change the state within an instance of AnimatedObject, and the interpreter makes use of 
 * the instances of this class stored within the animated object table to perform an animation
 * cycle.
 */
public class AnimatedObject implements Comparable<AnimatedObject> {

    /**
     * Number of animate cycles between moves of the AnimatedObject. Set by step.time action command.
     */
    public int stepTime;

    /**
     * Count down from StepTime for determining when the AnimatedObject will move. Initially set 
     * by step.time and it then counts down from there on each animate cycle, resetting back to 
     * the StepTime value when it hits zero.
     */
    public int stepTimeCount;
    
    /**
     * The index of this AnimatedObject in the animated object table. Set to -1 for add.to.pic objects.
     */
    public byte objectNumber;

    /**
     * Current X position of this AnimatedObject.
     */
    public short x;

    /**
     * Current Y position of this AnimatedObject.
     */
    public short y;

    /**
     * The current view number for this AnimatedObject.
     */
    public int currentView;

    /**
     * The View currently being used by this AnimatedObject.
     */
    public View view() { return state.views[currentView]; }

    /**
     * The current loop number within the View.
     */
    public int currentLoop;

    /**
     * The number of loops in the View.
     */
    public int numberOfLoops() { return view().loops.size(); }

    /**
     * The Loop that is currently cycling for this AnimatedObject.
     */
    public Loop loop() { return (Loop)view().loops.get(currentLoop); }

    /**
     * The current cell number within the loop.
     */
    public int currentCel;

    /**
     * The number of cels in the current loop.
     */
    public int numberOfCels() { return loop().cels.size(); }

    /**
     * The Cel currently being displayed.
     */
    public Cel cel() { return (Cel)loop().cels.get(currentCel); }

    /**
     * The previous Cel that was displayed.
     */
    public Cel previousCel;

    /**
     * The background save area for this AnimatedObject.
     */
    public SaveArea saveArea;

    /**
     * Previous X position.
     */
    public short prevX;

    /**
     * Previous Y position.
     */
    public short prevY;
    
    /**
     * X dimension of the current cel.
     */
    public short xSize() { return (short)cel().getWidth(); }

    /**
     * Y dimension of the current cel.
     */
    public short ySize() { return (short)cel().getHeight(); }

    /**
     * Distance that this AnimatedObject will move on each move.
     */
    public int stepSize;

    /**
     * The number of animate cycles between changing to the next cel in the current 
     * loop. Set by the cycle.time action command.
     */
    public int cycleTime;

    /**
     * Count down from CycleTime for determining when the AnimatedObject will cycle to the next
     * cel in the loop. Initially set by cycle.time and it then counts down from there on each
     * animate cycle, resetting back to the CycleTime value when it hits zero.
     */
    public int cycleTimeCount;

    /**
     * The AnimatedObject's direction.
     */
    public byte direction;

    /**
     * The AnimatedObject's motion type.
     */
    public MotionType motionType;

    /**
     * The AnimatedObject's cycling type.
     */
    public CycleType cycleType;

    /**
     * The priority band value for this AnimatedObject.
     */
    public byte priority;

    /**
     * The control colour of the box around the base of add.to.pic objects. Not application
     * to normal AnimatedObjects.
     */
    public byte controlBoxColour;

    /**
     * true if AnimatedObject is drawn on the screen; otherwise false;
     */
    public boolean drawn;

    /**
     * true if the AnimatedObject should ignore blocks; otherwise false. Ignoring blocks
     * means that it can pass black priority one lines and also script blocks. Set to true
     * by the ignore.blocks action command. Set to false by the observe.blocks action 
     * command.
     */
    public boolean ignoreBlocks;

    /**
     * true if the AnimatedObject has fixed priority; otherwise false. Set to true by the
     * set.priority action command. Set to false by the release.priority action command.
     */
    public boolean fixedPriority;

    /**
     * true if the AnimatedObject should ignore the horizon; otherwise false. Set to true 
     * by the ignore.horizon action command. Set to false by the observe.horizon action
     * command.
     */
    public boolean ignoreHorizon;

    /**
     * true if the AnimatedObject should be updated; otherwise false.
     */
    public boolean update;

    /**
     * true if the AnimatedObject should be cycled; otherwise false.
     */
    public boolean cycle;

    /**
     * true if the AnimatedObject can move; otherwise false.
     */
    public boolean animated;

    /**
     * true if the AnimatedObject is blocked; otherwise false.
     */
    public boolean blocked;

    /**
     * true if the AnimatedObject must stay entirely on water; otherwise false.
     */
    public boolean stayOnWater;

    /**
     * true if the AnimatedObject must not be entirely on water; otherwise false.
     */
    public boolean stayOnLand;

    /**
     * true if the AnimatedObject is ignoring collisions with other AnimatedObjects; otherwise false.
     */
    public boolean ignoreObjects;

    /**
     * true if the AnimatedObject is being repositioned in this cycle; otherwise false.
     */
    public boolean repositioned;

    /**
     * true if the AnimatedObject should not have the cel advanced in this loop; otherwise false.
     */
    public boolean noAdvance;

    /**
     * true if the AnimatedObject should not have the loop fixed; otherwise false. Having 
     * the loop fixed means that it will not adjust according to the direction. Set to 
     * true by the fix.loop action command. Set to false by the release.loop action command.
     */
    public boolean fixedLoop;

    /**
     * true if the AnimatedObject did not move in the last animation cycle; otherwise false.
     */
    public boolean stopped;

    /**
     * Miscellaneous motion parameter 1. Used by Wander, MoveTo, and Follow.
     */
    public short motionParam1;

    /**
     * Miscellaneous motion parameter 2.
     */
    public short motionParam2;

    /**
     * Miscellaneous motion parameter 3.
     */
    public short motionParam3;

    /**
     * Miscellaneous motion parameter 4.
     */
    public short motionParam4;

    /**
     * The GameState class holds all of the data and state for the Game currently 
     * being run by the interpreter.
     */
    private GameState state;

    /**
     *  Constructor for AnimatedObject.
     *
     * @param state
     * @param objectNum
     */
    public AnimatedObject(GameState state, int objectNum) {
        this.state = state;
        this.objectNumber = (byte)objectNum;
        this.saveArea = new SaveArea();
        reset(true);
    }

    /**
     * Resets the AnimatedObject back to its initial state.
     */
    public void reset() {
        reset(false);
    }
    
    /**
     * Resets the AnimatedObject back to its initial state.
     * 
     * @param fullReset true if it should be a full reset; otherwise false.
     */
    public void reset(boolean fullReset) {
        animated = false;
        drawn = false;
        update = true;

        previousCel = null;
        saveArea.visBackPixels = null;
        saveArea.priBackPixels = null;

        stepSize = 1;
        cycleTime = 1;
        cycleTimeCount = 1;
        stepTime = 1;
        stepTimeCount = 1;

        // A full reset is to go back to the initial state, whereas a normal reset is
        // simply for changing rooms.
        if (fullReset) {
            this.blocked = false;
            this.controlBoxColour = 0;
            this.currentCel = 0;
            this.currentLoop = 0;
            this.currentView = 0;
            this.cycle = false;
            this.cycleType = CycleType.NORMAL;
            this.direction = 0;
            this.fixedLoop = false;
            this.fixedPriority = false;
            this.ignoreBlocks = false;
            this.ignoreHorizon = false;
            this.ignoreObjects = false;
            this.motionParam1 = 0;
            this.motionParam2 = 0;
            this.motionParam3 = 0;
            this.motionParam4 = 0;
            this.motionType = MotionType.NORMAL;
            this.noAdvance = false;
            this.prevX = this.x = 0;
            this.prevY = this.y = 0;
            this.priority = 0;
            this.repositioned = false;
            this.stayOnLand = false;
            this.stayOnWater = false;
            this.stopped = false;
        }
    }
    
    /**
     * Updates the AnimatedObject's Direction based on its current MotionType.
     */
    public void updateDirection() {
        if (animated && update && drawn && (stepTimeCount == 1)) {
            switch (motionType) {
                case WANDER:
                    wander();
                    break;

                case FOLLOW:
                    follow();
                    break;

                case MOVE_TO:
                    moveTo();
                    break;
                    
                case NORMAL:
                    // Nothing to do.
                    break;
            }

            // If no blocks are in effect, clear the 'blocked' flag.  Otherwise,
            // if object must observe blocks, check for blocking.
            if (!state.blocking) {
                blocked = false;
            }
            else if (!ignoreBlocks && (direction != 0)) {
                checkBlock();
            }
        }
    }

    /**
     * Starts the Wander motion for this AnimatedObject.
     */
    public void startWander() {
        if (this == state.ego) {
            state.userControl = false;
        }
        this.motionType = MotionType.WANDER;
        this.update = true;
    }

    /**
     * If the AnimatedObject has stopped, but the motion type is Wander, then this
     * method picks a random direction and distance.
     * 
     * Note: motionParam1 is used to track the distance.
     */
    private void wander() {
        // Wander uses general purpose motion parameter 1 for the distance.
        if ((motionParam1-- == 0) || stopped) {
            direction = (byte)state.random.nextInt(9);

            // If the AnimatedObject is ego, then set the EGODIR var.
            if (objectNumber == 0) {
                state.setVar(Defines.EGODIR, direction);
            }

            motionParam1 = (short)((state.random.nextInt((Defines.MAXDIST - Defines.MINDIST)) + Defines.MINDIST) & 0xFF);
        }
    }

    /**
     * New Direction matrix to support the MoveDirection method.
     */
    private static final byte[][] newdir = { {8, 1, 2}, {7, 0, 3}, {6, 5, 4} };

    /**
     * Return the direction from (oldx, oldy) to (newx, newy).  If the object is within
     * 'delta' of the position in both directions, return 0
     *
     * @param oldx 
     * @param oldy 
     * @param newx 
     * @param newy 
     * @param delta 
     * 
     * @return 
     */
    private byte moveDirection(short oldx, short oldy, short newx, short newy, short delta) {
        return (newdir[directionIndex(newy - oldy, delta)][directionIndex(newx - oldx, delta)]);
    }

    /**
     * Return 0, 1, or 2 depending on whether the difference between coords, d,
     * indicates that the coordinate should decrease, stay the same, or increase.
     * The return value is used as one of the indeces into 'newdir' above.
     * 
     * @param d 
     * @param delta 
     * 
     * @return 0, 1, or 2, as described in the summary above.
     */
    private byte directionIndex(int d, short delta) {
        byte index = 0;

        if (d <= -delta) {
            index = 0;
        }
        else if (d >= delta) {
            index = 2;
        }
        else {
            index = 1;
        }

        return index;
    }

    /**
     * Move this AnimatedObject towards ego.
     * 
     * motionParam1 (endDist): Distance from ego which is considered to be completion of the motion.
     * motionParam2 (endFlag): Flag to set on completion of the motion
     * motionParam3 (randDist): Distance to move in current direction (for random search)
     */
    private void follow() {
        int maxDist = 0;

        // Get coordinates of center of object's & ego's bases.
        short ecx = (short)(state.ego.x + (state.ego.xSize() / 2));
        short ocx = (short)(this.x + (this.xSize() / 2));

        // Get direction from object's center to ego's center.
        byte dir = moveDirection(ocx, this.y, ecx, state.ego.y, motionParam1);

        // If the direction is zero, the object and ego have collided, so signal completion.
        if (dir == 0) {
            this.direction = 0;
            this.motionType = MotionType.NORMAL;
            this.state.setFlag(this.motionParam2, true);
            return;
        }

        // If the object has not moved since last time, assume it is blocked and
        // move in a random direction for a random distance no greater than the
        // distance between the object and ego

        // NOTE: randDist = -1 indicates that this is initialization, and thus
        // we don't care about the previous position
        if (this.motionParam3 == -1) {
            this.motionParam3 = 0;
        }
        else if (this.stopped) {
            // Make sure that the object goes in some direction.
            direction = (byte)(state.random.nextInt(8) + 1);

            // Average the x and y distances to the object for movement limit.
            maxDist = (Math.abs(ocx - ecx) + Math.abs(this.y - state.ego.y)) / 2 + 1;

            // Make sure that the distance is at least the object stepsize.
            if (maxDist <= this.stepSize) {
                this.motionParam3 = (short)this.stepSize;
            }
            else {
                this.motionParam3 = (short)(state.random.nextInt((maxDist - this.stepSize)) + this.stepSize);
            }

            return;
        }

        // If 'randDist' is non-zero, keep moving the object in the current direction.
        if (this.motionParam3 != 0) {
            if ((this.motionParam3 -= this.stepSize) < 0) {
                // Down with the random movement.
                this.motionParam3 = 0;
            }
            return;
        }

        // Otherwise, just move the object towards ego.  Whew...
        this.direction = dir;
    }

    /**
     * Starts a Follow ego motion for this AnimatedObject.
     * 
     * @param dist Distance from ego which is considered to be completion of the motion.
     * @param completionFlag The number of the flag to set when the motion is completed.
     */
    public void startFollowEgo(int dist, int completionFlag) {
        this.motionType = MotionType.FOLLOW;

        // Distance from ego which is considered to be completion of the motion is the larger of 
        // the object's StepSize and the dist parameter.
        this.motionParam1 = (short)(dist > this.stepSize ? dist : this.stepSize);
        this.motionParam2 = (short)completionFlag;
        this.motionParam3 = -1;                  // 'follow' routine expects this.
        state.setFlag(completionFlag, false);     // Flag to set at completion.
        this.update = true;
    }

    /**
     * Move this AnimatedObject toward the target (xt, yt) position, as defined below:
     * 
     * motionParam1 (xt): Target X coordinate.
     * motionParam2 (yt): Target Y coordinate.
     * motionParam3 (oldStep): Old stepsize for this AnimatedObject.
     * motionParam4 (endFlag): Flag to set when this AnimatedObject reaches the target position.
     */
    public void moveTo() {
        // Get the direction to move.
        this.direction = moveDirection(this.x, this.y, this.motionParam1, this.motionParam2, (short)this.stepSize);

        // If this AnimatedObject is ego, set var[EGODIR]
        if (this.objectNumber == 0) {
            this.state.setVar(Defines.EGODIR, this.direction);
        }

        // If 0, signal completion.
        if (this.direction == 0) {
            endMoveObj();
        }
    }

    /**
     * Starts the MoveTo motion for this AnimatedObject.
     *
     * @param x The x position to move to.
     * @param y The y position to move to.
     * @param stepSize The step size to use for the motion. If 0, then the current StepSize value for this AnimatedObject is used.
     * @param completionFlag The flag number to set when the motion has completed.
     */
    public void startMoveObj(int x, int y, int stepSize, int completionFlag) {
        this.motionType = MotionType.MOVE_TO;
        this.motionParam1 = (short)x;
        this.motionParam2 = (short)y;
        this.motionParam3 = (short)this.stepSize;
        if (stepSize != 0) {
            this.stepSize = stepSize;
        }
        this.motionParam4 = (short)completionFlag;
        state.setFlag(completionFlag, false);
        this.update = true;
        if (this == state.ego) {
            state.userControl = false;
        }
        this.moveTo();
    }

    /**
     * Ends the MoveTo motion for this AnimatedObject.
     */
    private void endMoveObj() {
        // Restore old step size.
        this.stepSize = this.motionParam3;

        // Set flag indicating completion.
        this.state.setFlag(this.motionParam4, true);

        // Set it back to normal motion.
        this.motionType = MotionType.NORMAL;

        // If this AnimatedObject is ego, then give back user control.
        if (this.objectNumber == 0) {
            state.userControl = true;
            state.setVar(Defines.EGODIR, 0);
        }
    }

    /**
     * A block is in effect and the object must observe blocks. Check to see
     * if the object can move in its current direction.
     */
    private void checkBlock() {
        boolean objInBlock;
        short ox, oy;

        // Get obj coord into temp vars and determine if the object is
        // currently within the block.
        ox = this.x;
        oy = this.y;

        objInBlock = inBlock(ox, oy);

        // Get object coordinate after moving.
        switch (this.direction) {
            case 1:
                oy -= this.stepSize;
                break;

            case 2:
                ox += this.stepSize;
                oy -= this.stepSize;
                break;

            case 3:
                ox += this.stepSize;
                break;

            case 4:
                ox += this.stepSize;
                oy += this.stepSize;
                break;

            case 5:
                oy += this.stepSize;
                break;

            case 6:
                ox -= this.stepSize;
                oy += this.stepSize;
                break;

            case 7:
                ox -= this.stepSize;
                break;

            case 8:
                ox -= this.stepSize;
                oy -= this.stepSize;
                break;
        }

        // If moving the object will not change its 'in block' status, let it move.
        if (objInBlock == inBlock(ox, oy)) {
            this.blocked = false;
        }
        else {
            this.blocked = true;
            this.direction = 0;

            // When Ego is the blocked object also set ego's direction to zero.
            if (this.objectNumber == 0) {
                state.setVar(Defines.EGODIR, 0);
            }
        }
    }

    /**
     * Tests if the currently active block contains the given X/Y position. Ths method should
     * not be called unless a block has been set.
     * 
     * @param x The X position to test.
     * @param y The Y position to test.
     * 
     * @return
     */ 
    private boolean inBlock(short x, short y) {
        return (x > state.blockUpperLeftX && x < state.blockLowerRightX && y > state.blockUpperLeftY && y < state.blockLowerRightY);
    }

    private static short[] xs = { 0, 0, 1, 1, 1, 0, -1, -1, -1 };
    private static short[] ys = { 0, -1, -1, 0, 1, 1, 1, 0, -1 };

    /**
     * Updates this AnimatedObject's position on the screen according to its current state.
     */
    public void updatePosition() {
        if (animated && update && drawn) {
            // Decrement the move clock for this object.  Don't move the object unless
            // the clock has reached 0.
            if ((stepTimeCount != 0) && (--stepTimeCount != 0)) return;

            // Reset the move clock.
            stepTimeCount = stepTime;

            // Clear border collision flag.
            byte border = 0;

            short ox = this.x;
            short px = this.x;
            short oy = this.y;
            short py = this.y;
            byte od = 0;
            short os = 0;

            // If object has not been repositioned, move it.
            if (!this.repositioned) {
                od = this.direction;
                os = (short)this.stepSize;
                ox += (short)(xs[od] * os);
                oy += (short)(ys[od] * os);
            }

            // Check for object border collision.
            if (ox < Defines.MINX) {
                ox = Defines.MINX;
                border = Defines.LEFT;
            }
            else if (ox + this.xSize() > Defines.MAXX + 1) {
                ox = (short)(Defines.MAXX + 1 - this.xSize());
                border = Defines.RIGHT;
            }
            if (oy - this.ySize() < Defines.MINY - 1) {
                oy = (short)(Defines.MINY - 1 + this.ySize());
                border = Defines.TOP;
            }
            else if (oy > Defines.MAXY) {
                oy = Defines.MAXY;
                border = Defines.BOTTOM;
            }
            else if (!ignoreHorizon && (oy <= state.horizon)) {
                oy = (short)(state.horizon + 1);
                border = Defines.TOP;
            }

            // Update X and Y to the new position.
            this.x = ox;
            this.y = oy;

            // If object can't be in this position, then move back to previous
            // position and clear the border collision flag
            if (collide() || !canBeHere()) {
                this.x = px;
                this.y = py;
                border = 0;

                // Make sure that this position is OK
                findPosition();
            }

            // If the object hit the border, set the appropriate flags.
            if (border > 0) {
                if (this.objectNumber == 0) {
                    state.setVar(Defines.EGOEDGE, border);
                }
                else {
                    state.setVar(Defines.OBJHIT, this.objectNumber);
                    state.setVar(Defines.OBJEDGE, border);
                }

                // If the object was on a 'moveobj', set the move as finished.
                if (this.motionType == MotionType.MOVE_TO) {
                    endMoveObj();
                }
            }

            // If object was not to be repositioned, it can be repositioned from now on.
            this.repositioned = false;
        }
    }

    /**
     * Return true if the object's position puts it on the screen; false otherwise.
     * 
     * @return true if the object's position puts it on the screen; false otherwise.
     */
    private boolean goodPosition() {
        return ((this.x >= Defines.MINX) && ((this.x + this.xSize()) <= Defines.MAXX + 1) && 
            ((this.y - this.ySize()) >= Defines.MINY - 1) && (this.y <= Defines.MAXY) &&
            (this.ignoreHorizon || this.y > state.horizon));
    }

    /**
     * Find a position for this AnimatedObject where it does not collide with any
     * unappropriate objects or priority regions.  If the object can't be in
     * its current position, then start scanning in a spiral pattern for a position
     * at which it can be placed.
     */
    public void findPosition() {
        // Place Y below horizon if it is above it and is not ignoring the horizon.
        if ((this.y <= state.horizon) && !this.ignoreHorizon) {
            this.y = (short)(state.horizon + 1);
        }

        // If current position is OK, return.
        if (goodPosition() && !collide() && canBeHere()) {
            return;
        }

        // Start scan.
        int legLen = 1, legDir = 0, legCnt = 1;

        while (!goodPosition() || collide() || !canBeHere()) {
            switch (legDir) {
                case 0:         // Move left.
                    --this.x;

                    if (--legCnt == 0)
                    {
                        legDir = 1;
                        legCnt = legLen;
                    }
                    break;

                case 1:         // Move down.
                    ++this.y;

                    if (--legCnt == 0)
                    {
                        legDir = 2;
                        legCnt = ++legLen;
                    }
                    break;

                case 2:         // Move right.
                    ++this.x;

                    if (--legCnt == 0)
                    {
                        legDir = 3;
                        legCnt = legLen;
                    }
                    break;

                case 3:         // Move up.
                    --this.y;

                    if (--legCnt == 0)
                    {
                        legDir = 0;
                        legCnt = ++legLen;
                    }
                    break;
            }
        }
    }

    /**
     * Checks if this AnimatedObject has collided with another AnimatedObject.
     * 
     * @return true if collided with another AnimatedObject; otherwise false.
     */
    private boolean collide() {
        // If AnimatedObject is ignoring objects this return false.
        if (this.ignoreObjects) {
            return false;
        }

        for (AnimatedObject otherObj : state.animatedObjects) {
            // Collision with another object if:
            //    - other object is animated and drawn
            //    - other object is not ignoring objects
            //    - other object is not this object
            //    - the two objects have overlapping baselines
            if (otherObj.animated && otherObj.drawn && 
                !otherObj.ignoreObjects && 
                (this.objectNumber != otherObj.objectNumber) && 
                (this.x + this.xSize() >= otherObj.x) && 
                (this.x <= otherObj.x + otherObj.xSize()))

                // At this point, the two objects have overlapping
                // x coordinates. A collision has occurred if they have
                // the same y coordinate or if the object in question has
                // moved across the other object in the last animation cycle
                if ((this.y == otherObj.y) || 
                    (this.y > otherObj.y && this.prevY < otherObj.prevY) || 
                    (this.y < otherObj.y && this.prevY > otherObj.prevY)) {
                
                    return true;
                }
        }

        return false;
    }

    /**
     * For the given y value, calculates what the priority value should be.
     * 
     * @param y 
     * 
     * @return
     */ 
    private byte calculatePriority(int y) {
        return (byte)(y < state.priorityBase ? Defines.BACK_MOST_PRIORITY : (byte)(((y - state.priorityBase) / ((168.0 - state.priorityBase) / 10.0f)) + 5));
    }
    
    /**
     * Return the effective Y for this Animated Object, which is Y if the priority is not fixed, or if it
     * is fixed then is the value corresponding to the start of the fixed priority band.
     * 
     */
    private short effectiveY() {
        // IMPORTANT: When in fixed priority mode, it uses the "top" of the priority band, not the bottom, i.e. the "start" is the top.
        return (fixedPriority ? (short)(state.priorityBase + Math.ceil(((168.0 - state.priorityBase) / 10.0f) * (priority - Defines.BACK_MOST_PRIORITY - 1))) : y);
    }
    
    /**
     * Checks if this AnimatedObject can be in its current position according to
     * the control lines. Normally this method would be invoked immediately after
     * setting its position to a newly calculated position.
     * 
     * There are a number of side effects to calling this method, and in fact 
     * it is responsible for performing these updates:
     * 
     * - It sets the priority value for the current Y position.
     * - It sets the on.water flag, if applicable.
     * - It sets the hit.special flag, if applicable.
     * 
     * @return true if it can be in the current position; otherwise false.
     */
    private boolean canBeHere() {
        boolean canBeHere = true;
        boolean entirelyOnWater = false;
        boolean hitSpecial = false;

        // If the priority is not fixed, calculate the priority based on current Y position.
        if (!this.fixedPriority) {
            // NOTE: The following table only applies to games that don't support the ability to change the PriorityBase.
            // Priority Band   Y range
            // ------------------------
            //       4 -
            //       5          48 - 59
            //       6          60 - 71
            //       7          72 - 83
            //       8          84 - 95
            //       9          96 - 107
            //      10         108 - 119
            //      11         120 - 131
            //      12         132 - 143
            //      13         144 - 155
            //      14         156 - 167
            //      15            168
            // ------------------------
            this.priority = calculatePriority(this.y);
        }

        // Priority 15 skips the whole base line testing. None of the control lines
        // have any affect.
        if (this.priority != 15) {
            // Start by assuming we're on water. Will be set false if it turns out we're not.
            entirelyOnWater = true;

            // Loop over the priority screen pixels for the area covered by this
            // object's base line.
            int startPixelPos = (y * 160) + x;
            int endPixelPos = startPixelPos + xSize();

            for (int pixelPos = startPixelPos; pixelPos < endPixelPos; pixelPos++) {
                // Get the priority screen priority value for this pixel of the base line.
                int priority = state.controlPixels[pixelPos];

                if (priority != 3) {
                    // This pixel is not water (i.e. not 3), so it can't be entirely on water.
                    entirelyOnWater = false;

                    if (priority == 0) {
                        // Permanent block.
                        canBeHere = false;
                        break;
                    }
                    else if (priority == 1) {
                        // Blocks if the AnimatedObject isn't ignoring blocks.
                        if (!ignoreBlocks) {
                            canBeHere = false;
                            break;
                        }
                    }
                    else if (priority == 2) {
                        hitSpecial = true;
                    }
                }
            }

            if (entirelyOnWater) {
                if (this.stayOnLand) {
                    // Must not be entirely on water, so can't be here.
                    canBeHere = false;
                }
            }
            else {
                if (this.stayOnWater) {
                    canBeHere = false;
                }
            }
        }

        // If the object is ego then we need to determine the on.water and hit.special flag values.
        if (this.objectNumber == 0) {
            state.setFlag(Defines.ONWATER, entirelyOnWater);
            state.setFlag(Defines.HITSPEC, hitSpecial);
        }

        return canBeHere;
    }

    // Object views -- Same, Right, Left, Front, Back.
    private static final byte S = 4;
    private static final byte R = 0;
    private static final byte L = 1;
    private static final byte F = 2;
    private static final byte B = 3;
    private static byte[] twoLoop = { S, S, R, R, R, S, L, L, L };
    private static byte[] fourLoop = { S, B, R, R, R, F, L, L, L };

    /**
     * Updates the loop and cel numbers based on the AnimatedObjects current state.
     */
    public void updateLoopAndCel() {
        byte newLoop = 0;

        if (animated && update && drawn) {
            // Get the appropriate loop based on the current direction.
            newLoop = S;

            if (!fixedLoop) {
                if (numberOfLoops() == 2 || numberOfLoops() == 3) {
                    newLoop = twoLoop[direction];
                }
                else if (numberOfLoops() == 4) {
                    newLoop = fourLoop[direction];
                }
                else if ((numberOfLoops() > 4) && (state.gameId.equals("KQ4"))) {
                    // Main Ego View (0) in KQ4 has 5 loops, but is expected to automatically change
                    // loop in sync with the Direction, in the same way as if it had only 4 loops.
                    newLoop = fourLoop[direction];
                }
            }

            // If the object is to move in this cycle and the loop has changed, point to the new loop.
            if ((stepTimeCount == 1) && (newLoop != S) && (currentLoop != newLoop)) {
                setLoop(newLoop);
            }

            // If it is time to cycle the object, advance it's cel.
            if (cycle && (cycleTimeCount > 0) && (--cycleTimeCount == 0)) {
                advanceCel();

                cycleTimeCount = cycleTime;
            }
        }
    }

    /**
     * Determine which cel of an object to display next.
     */
    public void advanceCel() {
        int theCel;
        int lastCel;

        if (noAdvance) {
            noAdvance = false;
            return;
        }

        // Advance to the next cel in the loop.
        theCel = currentCel;
        lastCel = (numberOfCels() - 1);

        switch (cycleType) {
            case NORMAL:
                // Move to the next sequential cel.
                if (++theCel > lastCel) {
                    theCel = 0;
                }
                break;

            case END_LOOP:
                // Advance to the end of the loop, set flag in parms[0] when done
                if (theCel >= lastCel || ++theCel == lastCel) {
                    state.setFlag(motionParam1, true);
                    cycle = false;
                    direction = 0;
                    cycleType = CycleType.NORMAL;
                }
                break;

            case REVERSE_LOOP:
                // Move backwards, celwise, until beginning of loop, then set flag.
                if (theCel == 0 || --theCel == 0) {
                    state.setFlag(motionParam1, true);
                    cycle = false;
                    direction = 0;
                    cycleType = CycleType.NORMAL;
                }
                break;

            case REVERSE:
                // Cycle continually, but from end of loop to beginning.
                if (theCel > 0) {
                    --theCel;
                }
                else {
                    theCel = lastCel;
                }
                break;
        }

        // Get pointer to the new cel and set cel dimensions.
        setCel(theCel);
    }

    /**
     * Adds this AnimatedObject as a permanent part of the current picture. If the priority parameter
     * is 0, the object's priority is that of the priority band in which it is placed; otherwise it
     * will be set to the specified priority value. If the controlBoxColour parameter is below 4, 
     * then a control line box is added to the control screen of the specified control colour value,
     * which extends from the object's baseline to the bottom of the next lowest priority band. If
     * this control box priority is set to 0, then obviously this would prevent animated objects from
     * walking through it. The other 3 control colours have their normal behaviours as well. The
     * add.to.pic objects ignore all control lines, all base lines of other objects, and the "block"
     * if one is active...   i.e. it can go anywhere in the picture. Once added, it is not animated
     * and cannot be erased ecept by drawing something over it. It effectively becomes part of the 
     * picture.
     * 
     * @param viewNum 
     * @param loopNum 
     * @param celNum 
     * @param x 
     * @param y 
     * @param priority 
     * @param controlBoxColour 
     * @param pixels 
     */
    public void addToPicture(int viewNum, int loopNum, int celNum, int x, int y, int priority, int controlBoxColour, PixelData pixelData) {
        // Add the add.to.pic details to the script event buffer.
        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.ADD_TO_PIC, 0, new byte[] {
            (byte)viewNum, (byte)loopNum, (byte)celNum, (byte)x, (byte)y, (byte)(priority | (controlBoxColour << 4))
        });

        // Set the view, loop, and cel to those specified.
        setView(viewNum);
        setLoop(loopNum);
        setCel(celNum);

        // Set PreviousCel to current Cel for Show call.
        this.previousCel = this.cel();

        // Place the add.to.pic at the specified position. This may not be fully within the
        // screen bounds, so a call below to FindPosition is made to resolve this.
        this.x = this.prevX = (short)x;
        this.y = this.prevY = (short)y;

        // In order to make use of FindPosition, we set these flags to disable certain parts
        // of the FindPosition functionality that don't apply to add.to.pic objects.
        this.ignoreHorizon = true;
        this.fixedPriority = true;
        this.ignoreObjects = true;

        // And we set the priority temporarily to 15 so that when FindPosition is doing its thing,
        // the control lines will be ignored, as they have no effect on add.to.pic objects.
        this.priority = 15;

        // Now we call FindPosition to adjust the object's position if it has been placed either 
        // partially or fully outside of the picture area.
        findPosition();

        // Having checked and (if appropriate) adjusted the position, we can now work out what the
        // object priority should be.
        if (priority == 0) {
            // If the specified priority is 0, it means that the priority should be calculated 
            // from the object's Y position as would normally happen if its priority is not fixed.
            this.priority = calculatePriority(this.y);
        }
        else {
            // Otherwise it will be set to the specified value.
            this.priority = (byte)priority;
        }

        this.controlBoxColour = (byte)controlBoxColour;

        // Draw permanently to the CurrentPicture, including the control box.
        draw(state.currentPicture);

        // Restore backgrounds, add add.to.pic to VisualPixels, then redraw AnimatedObjects and show updated area.
        state.restoreBackgrounds();
        draw();
        state.drawObjects();
        show(pixelData);
    }

    /**
     * Set the Cel of this AnimatedObject to the given cel number.
     * 
     * @param celNum The cel number within the current Loop to set the Cel to.
     */
    public void setCel(int celNum) {
        if(celNum >= numberOfCels()) {
            // celNum is out of bounds for the current view/loop
            return;
        } 
        
        // Set the cel number. 
        this.currentCel = celNum;

        // The border collision can only be performed if a valid combination of loops and cels has been set.
        if ((this.currentLoop < this.numberOfLoops()) && (this.currentCel < this.numberOfCels())) {
            // Make sure that the new cel size doesn't cause a border collision.
            if (this.x + this.xSize() > Defines.MAXX + 1) {
                // Don't let the object move.
                this.repositioned = true;
                this.x = (short)(Defines.MAXX - this.xSize());
            }

            if (this.y - this.ySize() < Defines.MINY - 1) {
                this.repositioned = true;
                this.y = (short)(Defines.MINY - 1 + this.ySize());

                if (this.y <= state.horizon && !this.ignoreHorizon) {
                    this.y = (short)(state.horizon + 1);
                }
            }
        }
    }

    /**
     * Set the loop of this AnimatedObject to the given loop number.
     * 
     * @param loopNum The loop number within the current View to set the Loop to.
     */
    public void setLoop(int loopNum) {
        if(loopNum >= numberOfLoops()) {
            // loopNum is out of bounds for the current view
            return;
        } 

        this.currentLoop = loopNum;

        // If the current cel # is greater than the cel count for this loop, set
        // it to 0, otherwise leave it alone. Sometimes the loop number is set before
        // the associated view number is set. We allow for this in the check below.
        if ((this.currentLoop >= this.numberOfLoops()) || (this.currentCel >= this.numberOfCels())) {
            this.currentCel = 0;
        }

        this.setCel(this.currentCel);
    }

    /**
     * Set the number of the View for this AnimatedObject to use.
     * 
     * @param viewNum The number of the View for this AnimatedObject to use.
     */
    public void setView(int viewNum) {
        this.currentView = viewNum;

        // If the current loop is greater than the number of loops for the view,
        // set the loop number to 0.  Otherwise, leave it alone.
        setLoop(currentLoop >= numberOfLoops()? 0 : currentLoop);
    }

    /**
     * Performs an animate.obj on this AnimatedObject.
     */
    public void animate() {
        if (!animated) {
            // Most flags are reset to false.
            this.ignoreBlocks = false;
            this.fixedPriority = false;
            this.ignoreHorizon = false;
            this.cycle = false;
            this.blocked = false;
            this.stayOnLand = false;
            this.stayOnWater = false;
            this.ignoreObjects = false;
            this.repositioned = false;
            this.noAdvance = false;
            this.fixedLoop = false;
            this.stopped = false;

            // But these ones are specifying set to true.
            this.animated = true;
            this.update = true;
            this.cycle = true;

            this.motionType = MotionType.NORMAL;
            this.cycleType = CycleType.NORMAL;
            this.direction = 0;
        }
    }

    /**
     * Repositions the object by the deltaX and deltaY values.
     * 
     * @param deltaX Delta for the X position (signed, where negative is to the left)
     * @param deltaY Delta for the Y position (signed, where negative is to the top)
     */
    public void reposition(byte deltaX, byte deltaY) {
        // IMPORTANT: The deltaX and deltaY really are signed bytes.
        this.repositioned = true;

        if ((deltaX < 0) && (this.x < -deltaX)) {
            this.x = 0;
        }
        else {
            this.x = (short)(this.x + deltaX);
        }

        if ((deltaY < 0) && (this.y < -deltaY)) {
            this.y = 0;
        }
        else {
            this.y = (short)(this.y + deltaY);
        }

        // Make sure that this position is OK
        findPosition();
    }

    /**
     * Calculates the distance between this AnimatedObject and the given AnimatedObject.
     * 
     * @param aniObj The AnimatedObject to calculate the distance to.
     * 
     * @return
     */ 
    public int distance(AnimatedObject aniObj) {
        if (!this.drawn || !aniObj.drawn) {
            return Defines.MAXVAR;
        }
        else {
            int dist = Math.abs((this.x + this.xSize() / 2) - (aniObj.x + aniObj.xSize() / 2)) + Math.abs(this.y - aniObj.y);
            return ((dist > 254) ? 254 : dist);
        }
    }

    /**
     * Draws this AnimatedObject to the pixel arrays of the given Picture. This is intended for use by 
     * add.to.pic objects, which is a specialist static type of AnimatedObject that becomes a permanent
     * part of the Picture.
     * 
     * @param picture 
     */
    public void draw(Picture picture) {
        Cel cel = cel();
        int cellWidth = cel.getWidth();
        int cellHeight = cel.getHeight();
        
        // The cellPixels array is already in ARGB format.
        int[] cellPixels = cel.getPixelData();
        
        // The visualPixels array is already in ARGB format.
        int[] visualPixels = picture.getVisualPixels();
        
        // The priorityPixels array is in index format (i.e. 0-15)
        int[] priorityPixels = picture.getPriorityPixels();
        
        // Get the transparency colour. We'll use this to ignore pixels this colour.
        int transparentPixelRGB = this.cel().getTransparentPixel();

        // Calculate starting position within the pixel arrays.
        int aniObjTop = ((this.y - cellHeight) + 1);
        int screenPos = (aniObjTop * 160) + this.x;
        int screenLineAdd = 160 - cellWidth;
        int cellPos = 0;
        int cellXAdd = 1;
        int cellYAdd = 0;;

        // Iterate over each of the pixels and decide if the priority screen allows the pixel
        // to be drawn or not when adding them in to the VisualPixels and PriorityPixels arrays. 
        for (int y = 0; y < cellHeight; y++, screenPos += screenLineAdd, cellPos += cellYAdd) {
            for (int x = 0; x < cellWidth; x++, screenPos++, cellPos += cellXAdd) {
                // Check that the pixel is within the bounds of the AGI picture area.
                if (((aniObjTop + y) >= 0) && ((aniObjTop + y) < 168) && ((this.x + x) >= 0) && ((this.x + x) < 160)) {
                    // Get the priority colour index for this position from the priority screen.
                    int priorityIndex = priorityPixels[screenPos];

                    // If this AnimatedObject's priority is greater or equal to the priority screen value
                    // for this pixel's position, then we'll draw it.
                    if (this.priority >= priorityIndex) {
                        // Get the colour index from the Cell bitmap pixels.
                        int cellPixelRGB = cellPixels[cellPos];

                        // If the colourIndex is not the transparent index, then we'll draw the pixel.
                        if (cellPixelRGB != transparentPixelRGB) {
                            visualPixels[screenPos] = cellPixelRGB;
                            //  Replace the priority pixel only if the existing one is not a special priority pixel (0, 1, 2)
                            if (priorityIndex > 2) {
                                priorityPixels[screenPos] = this.priority;
                            }
                        }
                    }
                }
            }
        }

        // Draw the control box.
        if (controlBoxColour <= 3) {
            // Calculate the height of the box.
            int yy = this.y;
            byte priorityHeight = 0;
            byte objPriorityForY = calculatePriority(this.y);
            do {
                priorityHeight++;
                if (yy <= 0) break;
                yy--;
            }
            while (calculatePriority(yy) == objPriorityForY);
            int height = (ySize() > priorityHeight ? priorityHeight : ySize());

            // Draw bottom line.
            for (int i = 0; i < xSize(); i++) {
                priorityPixels[(this.y * 160) + this.x + i] = controlBoxColour;
            }

            if (height > 1) {
                // Draw both sides.
                for (int i = 1; i < height; i++) {
                    priorityPixels[((this.y - i) * 160) + this.x] = controlBoxColour;
                    priorityPixels[((this.y - i) * 160) + this.x + xSize() - 1] = controlBoxColour;
                }

                // Draw top line.
                for (int i = 1; i < xSize() - 1; i++) {
                    priorityPixels[((this.y - (height - 1)) * 160) + this.x + i] = controlBoxColour;
                }
            }
        }
    }

    /**
     * Draws this AnimatedObject to the VisualPixels pixels array.
     */
    public void draw() {
        Cel cel = cel();
        int cellWidth = cel.getWidth();
        int cellHeight = cel.getHeight();
        int[] cellPixels = cel.getPixelData();
        
        // Get the transparency colour. We'll use this to ignore pixels this colour.
        int transparentPixelRGB = cel.getTransparentPixel();

        // Calculate starting screen offset. AGI pixels are 2x1 within the picture area.
        int aniObjTop = ((this.y - cellHeight) + 1);
        int screenPos = (aniObjTop * 320) + (this.x * 2);
        int screenLineAdd = 320 - (cellWidth << 1);

        // Calculate starting position within the priority screen.
        int priorityPos = (aniObjTop * 160) + this.x;
        int priorityLineAdd = 160 - cellWidth;
        int cellPos = 0;
        int cellXAdd = 1;
        int cellYAdd = 0;

        // Allocate new background pixel array for the current cell size.
        this.saveArea.visBackPixels = new int[cellWidth][cellHeight];
        this.saveArea.priBackPixels = new int[cellWidth][cellHeight];
        this.saveArea.x = this.x;
        this.saveArea.y = this.y;
        this.saveArea.width = cellWidth;
        this.saveArea.height = cellHeight;

        // Iterate over each of the pixels and decide if the priority screen allows the pixel
        // to be drawn or not. Deliberately tried to avoid multiplication within the loops.
        for (int y = 0; y < cellHeight; y++, screenPos += screenLineAdd, priorityPos += priorityLineAdd, cellPos += cellYAdd) {
            for (int x = 0; x < cellWidth; x++, screenPos += 2, priorityPos++, cellPos += cellXAdd) {
                // Check that the pixel is within the bounds of the AGI picture area.
                if (((aniObjTop + y) >= 0) && ((aniObjTop + y) < 168) && ((this.x + x) >= 0) && ((this.x + x) < 160)) {
                    // Store the background pixel. Should be the same colour in both pixels.
                    this.saveArea.visBackPixels[x][y] = state.visualPixels[screenPos];
                    this.saveArea.priBackPixels[x][y] = state.priorityPixels[priorityPos];

                    // Get the priority colour index for this position from the priority screen.
                    int priorityIndex = state.priorityPixels[priorityPos];

                    // If this AnimatedObject's priority is greater or equal to the priority screen value
                    // for this pixel's position, then we'll draw it.
                    if (this.priority >= priorityIndex) {
                        // Get the colour index from the Cell bitmap pixels.
                        int cellPixelRGB = cellPixels[cellPos];

                        // If the colourIndex is not the transparent index, then we'll draw the pixel.
                        if (cellPixelRGB != transparentPixelRGB) {
                            // Draw two pixels (due to AGI picture pixels being 2x1).
                            state.visualPixels[screenPos] = cellPixelRGB;
                            state.visualPixels[screenPos + 1] = cellPixelRGB;

                            // Priority screen is only stored 160x168 though.
                            state.priorityPixels[priorityPos] = this.priority;
                        }
                    }
                }
            }
        }
    }

    /**
     * Restores the current background pixels to the previous position of this AnimatedObject.
     */
    public void restoreBackPixels() {
        if ((saveArea.visBackPixels != null) && (saveArea.priBackPixels != null)) {
            int saveWidth = saveArea.width;
            int saveHeight = saveArea.height;
            int aniObjTop = ((saveArea.y - saveHeight) + 1);
            int screenPos = (aniObjTop * 320) + (saveArea.x * 2);
            int screenLineAdd = 320 - (saveWidth << 1);
            int priorityPos = (aniObjTop * 160) + saveArea.x;
            int priorityLineAdd = 160 - saveWidth;

            for (int y = 0; y < saveHeight; y++, screenPos += screenLineAdd, priorityPos += priorityLineAdd) {
                for (int x = 0; x < saveWidth; x++, screenPos += 2, priorityPos++) {
                    if (((aniObjTop + y) >= 0) && ((aniObjTop + y) < 168) && ((saveArea.x + x) >= 0) && ((saveArea.x + x) < 160)) {
                        state.visualPixels[screenPos] = saveArea.visBackPixels[x][y];
                        state.visualPixels[screenPos + 1] = saveArea.visBackPixels[x][y];
                        state.priorityPixels[priorityPos] = saveArea.priBackPixels[x][y];
                    }
                }
            }
        }
    }

    /**
     * Shows the AnimatedObject by blitting the bounds of its current and previous cels to the screen 
     * pixels. The include the previous cel so that we pick up the restoration of the save area.
     * 
     * @param pixels The screen pixels to blit the AnimatedObject to.
     */
    public void show(PixelData pixelData) {
        // We will only render an AnimatedObject to the screen if the picture is currently visible.
        if (state.pictureVisible) {
            // Work out the rectangle that covers the previous and current cells.
            int prevCelWidth = (this.previousCel != null ? this.previousCel.getWidth() : this.xSize());
            int prevCelHeight = (this.previousCel != null? this.previousCel.getHeight() : this.ySize());
            int prevX = (this.previousCel != null ? this.prevX : this.x);
            int prevY = (this.previousCel != null ? this.prevY : this.y);
            int leftmostX = Math.min(prevX, this.x);
            int rightmostX = Math.max(prevX + prevCelWidth, this.x + this.xSize()) - 1;
            int topmostY = Math.min(prevY - prevCelHeight, this.y - this.ySize()) + 1;
            int bottommostY = Math.max(prevY, this.y);

            // We no longer need the PreviousCel, so point it at the new one.
            this.previousCel = this.cel();

            int height = (bottommostY - topmostY) + 1;
            int width = ((rightmostX - leftmostX) + 1) * 2;
            int picturePos = (topmostY * 320) + (leftmostX * 2);
            int pictureLineAdd = 320 - width;
            int screenPos = picturePos + (state.pictureRow * 8 * 320);

            for (int y = 0; y < height; y++, picturePos += pictureLineAdd, screenPos += pictureLineAdd) {
                for (int x = 0; x < width; x++, screenPos++, picturePos++) {
                    if (((topmostY + y) >= 0) && ((topmostY + y) < 168) && ((leftmostX + x) >= 0) && ((leftmostX + x) < 320) && (screenPos >= 0) && (screenPos < 64000 /* 320*200 */)) {
                        pixelData.putPixel(screenPos, state.visualPixels[picturePos]);
                    }
                }
            }
        }
    }
    
    /**
     * Used to sort by drawing order when drawing AnimatedObjects to the screen. When 
     * invoked, it compares the other AnimatedObject with this one and says which is in
     * front and which is behind. Since we want to draw those with lowest priority first, 
     * and if their priority is equal then lowest Y, then this is what determines whether
     * we return a negative value, equal, or greater.
     * 
     * @param other The other AnimatedObject to compare this one to.
     */
    public int compareTo(AnimatedObject other) {
        if (this.priority < other.priority) {
            return -1;
        }
        else if (this.priority > other.priority) {
            return 1;
        }
        else {
            if (this.effectiveY() < other.effectiveY()) {
                return -1;
            }
            else if (this.effectiveY() > other.effectiveY()) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

    /**
     * Gets the core status of the object in the status string format used by the AGI
     * debug mode. 
     */
    public String getStatusStr() {
        return StringUtils.format(
            "Object %d:\nx: %d  xsize: %d\ny: %d  ysize: %d\npri: %d\nstepsize: %d",
            objectNumber, x, xSize(), y, ySize(), priority, stepSize);
    }
    
    /**
     * An enum that defines the types of motion that an AnimatedObject can have.
     */
    public enum MotionType {
       
        /**
         * AnimatedObject is using the normal motion.
         */
        NORMAL,

        /**
         * AnimatedObject randomly moves around the screen.
         */
        WANDER,

        /**
         * AnimatedObject follows another AnimatedObject.
         */
        FOLLOW,

        /**
         * AnimatedObject is moving to a given coordinate.
         */
        MOVE_TO
    }

    /**
     * An enum that defines the type of cel cycling that an AnimatedObject can have.
     */
    public enum CycleType {
       
        /**
         * Normal repetitive cycling of the AnimatedObject.
         */
        NORMAL,

        /**
         * Cycle to the end of the loop and then stop.
         */
        END_LOOP,

        /**
         * Cycle in reverse order to the start of the loop and then stop.
         */
        REVERSE_LOOP,

        /**
         * Cycle continually in reverse.
         */
        REVERSE
    }
}
