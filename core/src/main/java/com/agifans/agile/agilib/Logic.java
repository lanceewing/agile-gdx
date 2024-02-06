package com.agifans.agile.agilib;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agifans.agile.util.StringUtils;

public class Logic extends Resource {

    /**
     * At the top level, all Instructions are Actions. The Conditions only exist as 
     * members of an IfAction or OrCondition.
     */
    public List<Action> actions;

    /**
     * Holds the messages for this Logic.
     */
    public List<String> messages;

    /**
     *  A Lookup mapping between an address value and the index within the Actions List
     * of the Action that is at that address.
    */
    public Map<Integer, Integer> addressToActionIndex;

    /**
     * If this Logic includes a set.game.id action command, then this field will contain
     * the value of the associated message number. The main purpose of this field is to 
     * aid in obtaining the AGI game's identifier as quickly as possible. For AGI V3
     * games, this is easy, but for AGI V2 games, it is set with the set.game.id command.
     * So we either wait until that command is executed and get it then, or we grab it
     * while the LOGIC resource is being decoded. The latter is obviously earlier in the
     * process. 
     */
    private Integer gameIdMessageNum;
    
    /**
     * Whether the messages are crypted or not.
    */
    private boolean messagesCrypted;
    
    /**
     * Constructor for Logic.
     * 
     * @param rawData
     * @param messagesCrypted
     */
    public Logic(byte[] rawData, boolean messagesCrypted) {
        // A Logic is simply a collection of Actions and a collection of Messages.
        this.actions = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.addressToActionIndex = new HashMap<>();
        this.messagesCrypted = messagesCrypted;

        // Decode the raw LOGIC resource data into the Actions and Messages.
        decode(rawData);
    }
    
    /**
     * Decode the raw LOGIC resource data into the Actions and Messages.
     * 
     * @param rawData 
     */
    public void decode(byte[] rawData) {
        // Read the Instructions. The first two bytes are the length of the Instructions section.
        readActions(ByteBuffer.wrap(rawData, 2, ((rawData[0] & 0xFF) + ((rawData[1] & 0xFF) << 8))));

        // Read the messages.
        readMessages(rawData);
    }
    
    /**
     * If this LOGIC contained the set.game.id command, then this method will return
     * the game ID text; otherwise it returns null.
     * 
     * @return Either the game ID, or null if this LOGIC doesn't set the game id.
     */
    public String getGameId() {
        return (gameIdMessageNum != null? messages.get(gameIdMessageNum) : null);
    }
    
    /**
     * Reads all Action commands from the given Stream.
     * 
     * @param stream The Stream to read the Actions from.
     */
    private void readActions(ByteBuffer stream) {
        Action action;
        int actionNumber = 0;

        while ((action = readAction(stream)) != null) {
            actions.add(action);
            addressToActionIndex.put(action.address, actionNumber++);
        }
        
        // Some AGI LOGICs, e.g. 99 in XMAS card, do not have a return statement.
        if (actions.get(actions.size() - 1).operation.opcode != 0) {
            action = new Action(ACTION_OPERATIONS[0], new ArrayList<Operand>());
            action.address = stream.position() - 1;
            action.logic = this;
            actions.add(action);
            addressToActionIndex.put(action.address, actionNumber++);
        }
    }

    /**
     * Reads an Action from the given Stream. If the end of the Stream has been reached, 
     * will return null.
     * 
     * @param stream The Stream to read the Action from.
     * 
     * @return The Action that was read in, or null if the end of the Stream was reached.
     */
    private Action readAction(ByteBuffer stream) {
        Action action = null;
        int address = stream.position();
        int actionOpcode = readUnsignedByte(stream);

        if (actionOpcode >= 0) {
            if (actionOpcode == 0xFF) {       // IF
                List<Operand> operands = new ArrayList<Operand>();
                List<Condition> conditions = new ArrayList<Condition>();
                Condition condition = null;

                while ((condition = readCondition(stream, 0xFF)) != null) {
                    conditions.add(condition);
                }

                operands.add(new Operand(OperandType.TESTLIST, conditions));
                operands.add(new Operand(OperandType.ADDRESS, 
                        ((short)(readUnsignedByte(stream) + (readUnsignedByte(stream) << 8))) + 
                        stream.position()));
                action = new IfAction(operands);
            }
            else if (actionOpcode == 0xFE) {  // GOTO
                List<Operand> operands = new ArrayList<Operand>();
                operands.add(new Operand(OperandType.ADDRESS, 
                        ((short)(readUnsignedByte(stream) + (readUnsignedByte(stream) << 8))) + 
                        stream.position()));
                action = new GotoAction(operands);
            }
            else {
                // Otherwise it is a normal Action.
                Operation operation = ACTION_OPERATIONS[actionOpcode];
                List<Operand> operands = new ArrayList<Operand>();

                for (OperandType operandType : operation.operandTypes) {
                    operands.add(new Operand(operandType, readUnsignedByte(stream)));
                }

                action = new Action(operation, operands);
                
                // Capture the set.game.id message number, if this LOGIC includes it. This 
                // is a quick way to identify an AGIV2 game before the game starts. We could
                // also use the Detection class, but it is ultimately the set.game.id command
                // that is the master. Maybe we could fall back on the Detection data if a 
                // game doesn't set the game ID (e.g. an AGI fan game).
                if (actionOpcode == 143) {
                    gameIdMessageNum= operands.get(0).asByte();
                }
            }

            // Keep track of each Instruction's address and Logic as we read them in.
            action.address = address;
            action.logic = this;
        }

        return action;
    }

    /**
     * Reads a Condition from the given Stream. If the first byte read matches the endCode, then
     * we return null to indicate that there is no Condition to read. Conditions always appear 
     * within an IF block or an OR block, so the endCode will be either 0xFF (for if) or 0xFC (for
     * or).
     * 
     * @param stream The Stream to read from.
     * @param endCode The code that we return null for. Will be either 0xFF or 0xFC.
     * 
     * @return The Condition that was read if there was one to read; otherwise null if there wasn't one.
     */
    private Condition readCondition(ByteBuffer stream, int endCode) {
        Condition condition = null;
        int address = (int)stream.position() - 2;
        int conditionOpcode = readUnsignedByte(stream);

        if (conditionOpcode != endCode) {
            if (conditionOpcode == 0xFC) {        // OR
                List<Operand> operands = new ArrayList<Operand>();
                List<Condition> conditions = new ArrayList<Condition>();
                Condition orCondition = null;

                while ((orCondition = readCondition(stream, 0xFC)) != null) {
                    conditions.add(orCondition);
                }

                operands.add(new Operand(OperandType.TESTLIST, conditions));
                condition = new OrCondition(operands);
            }
            else if (conditionOpcode == 0xFD) {   // NOT
                List<Operand> operands = new ArrayList<Operand>();
                operands.add(new Operand(OperandType.TEST, readCondition(stream, 0xFF)));
                condition = new NotCondition(operands);
            }
            else if (conditionOpcode == 0x0E) {   // SAID
                // The said command has a variable number of 16 bit word numbers, so needs special handling.
                Operation operation = TEST_OPERATIONS[conditionOpcode];
                List<Operand> operands = new ArrayList<Operand>();
                List<Integer> wordNumbers = new ArrayList<Integer>();
                int numOfWords = readUnsignedByte(stream);

                for (int i=0; i < numOfWords; i++) {
                    wordNumbers.add(readUnsignedByte(stream) + (readUnsignedByte(stream) << 8));
                }

                operands.add(new Operand(OperandType.WORDLIST, wordNumbers));
                condition = new Condition(operation, operands);
            }
            else {
                // Otherwise it's a normal condition.
                Operation operation = TEST_OPERATIONS[conditionOpcode];
                List<Operand> operands = new ArrayList<Operand>();

                for (OperandType operandType : operation.operandTypes) {  // TODO: This is where the null reference is.
                    operands.add(new Operand(operandType, readUnsignedByte(stream)));
                }

                condition = new Condition(operation, operands);
            }

            // Keep track of each Instruction's address and Logic as we read them in.
            condition.address = address;
            condition.logic = this;
        }

        return condition;
    }

    /**
     * Reads the Logic's messages from the raw data.
     * 
     * @param rawData The raw data to read the messages from.
     */
    private void readMessages(byte[] rawData) {
        int messagesOffset = ((rawData[0] & 0xFF) + ((rawData[1] & 0xFF) << 8)) + 2;
        int numOfMessages = (rawData[messagesOffset + 0] & 0xFF);
        int startOfText = messagesOffset + 3 + (numOfMessages * 2);

        if (messagesCrypted) {
            // Decrypt the message text section.
            crypt(rawData, startOfText, rawData.length);
        }

        // Message numbers start at 1, so we'll set index 0 to empty.
        this.messages.add("");

        // Add each message to the Messages List.
        for (int messNum = 1, marker = messagesOffset + 3; messNum <= numOfMessages; messNum++, marker += 2) {
            // Calculate the start of this message text.
            int msgStart = (rawData[marker] & 0xFF) + ((rawData[marker + 1] & 0xFF) << 8);
            String msgText = "";

            // Message text will only exist for those where the start offset is greater than 0.
            if (msgStart > 0) {
                int msgEnd = (msgStart += (messagesOffset + 1));

                // Find the end of the message text. It is 0 terminated.
                while ((rawData[msgEnd++] & 0xFF) != 0) ;

                // Convert the byte data between the message start and end in to an ASCII string.
                msgText = StringUtils.getStringFromBytes(rawData, msgStart, msgEnd - msgStart - 1);
            }

            this.messages.add(msgText);
        }
    }
    
    /**
     * Reads a byte from the ByteBuffer then converts to unsigned int.
     * 
     * @param byteBuffer ByteBuffer to read the byte from.
     * 
     * @return An int containing the unsigned byte value, or -1 if end reached.
     */
    private int readUnsignedByte(ByteBuffer byteBuffer) {
        try {
            return ((int)byteBuffer.get() & 0xFF);
        } catch (BufferUnderflowException e) {
            return -1;
        }
    }
        
    /**
     * Represents an AGI Instruction, being an Operation and it's List of Operands. This class
     * is abstract since all Instructions will be either an Action or a Condition.
    */
    public abstract class Instruction {
        
        /**
         * The Operation for this Instruction.
         */
        public Operation operation;

        /**
         * The List of Operands for this Instruction.
         */
        public List<Operand> operands;

        /**
         * The address of this Instruction within the Logic file.
         */
        public int address;
        
        /**
         * Holds a reference to the Logic that this Instruction belongs to.
         */
        public Logic logic;

        /**
         * Constructor for Instruction.
         * 
         * @param operation The Operation for this Instruction.
         * @param operands The List of Operands for this Instruction.
         */
        public Instruction(Operation operation, List<Operand> operands) {
            this.operation = operation;
            this.operands = operands;
        }
        
        public String toString() {
            StringBuilder str = new StringBuilder();
            if (logic != null) {
                str.append("LOGIC.");
                str.append(logic.index);
                str.append(": Address ");
                str.append(address);
                str.append(": ");
            }
            str.append(operation.format);
            str.append(" ] ");
            for (Operand operand : operands) {
                str.append(operand.getValue().toString());
                str.append(", ");
            }
            return str.toString();
        }
    }

    /**
     * A Condition is a type of AGI Instruction that tests something and returns
     * a boolean value.
     */
    public class Condition extends Instruction {
        public Condition(Operation operation, List<Operand> operands) {
            super(operation, operands);
        }
    }

    /**
     * An Action is a type of AGI Instruction that performs an action.
     */
    public class Action extends Instruction {

        public Action(Operation operation, List<Operand> operands)  {
            super(operation, operands);
        }
        
        /**
         * Get the index of this Action within it's Logic's Action List.
         */
        public int getActionNumber() { 
            return this.logic.addressToActionIndex.get(this.address);
        }
    }

    /**
     * The JumpAction is an abstract base class of both the IfAction and GotoAction.
     */
    public abstract class JumpAction extends Action {
        
        public JumpAction(Operation operation, List<Operand> operands)  {
            super(operation, operands);
        }

        /**
         * Gets the index of the Action that this JumpAction jumps to.
         * 
         * @return The index of the Action that this JumpAction jumps to.
         */
        public int getDestinationActionIndex() {
            return this.logic.addressToActionIndex.get(this.getDestinationAddress());
        }

        /**
         * Gets the destination address of this JumpAction.
         *
         * @return The destination address of this JumpAction.
         */
        public abstract int getDestinationAddress();
    }

    /**
     * The IfAction is a special type of AGI Instruction that tests one or more Conditions
     * to decide whether to jump over the block of immediately following Actions. It's operands
     * are a List of Conditions and a jump address.
     */
    public class IfAction extends JumpAction {
        
        public IfAction(List<Operand> operands) {
            super(new Operation(255, "if(TESTLIST,ADDRESS)", "InstructionIf"), operands);
        }

        public int getDestinationAddress() {
            return this.operands.get(1).asInt();
        }
    }

    /**
     * The GotoAction is a special type of AGI Instruction that performs an unconditional
     * jump to a given address. It's one and only operand is the jump address. This Instruction
     * is mainly used for the 'else' keyword, but also for the 'goto' keyword.
     */
    public class GotoAction extends JumpAction {
        
        public GotoAction(List<Operand> operands)  {
            super(new Operation(254, "goto(ADDRESS)", "InstructionGoto"), operands);
        }

        public int getDestinationAddress() {
            return this.operands.get(0).asInt();
        }
    }

    /**
     * The NotCondition is a special type of AGI Instruction that tests that the test 
     * command immediately following it evaluates to false. It's one and only operand will
     * be a Condition, and that Condition cannot be an OrCondition.
     */
    public class NotCondition extends Condition {
        
        public NotCondition(List<Operand> operands)  {
            super(new Operation(253, "not(TEST)", "ExpressionNot"), operands);
        }
    }

    /**
     * The OrCondition is a special type of AGI Instruction that tests two or more
     * Conditions to see if at least one of them evaluates to true. It's operand is
     * a List of Conditions.
    */
    public class OrCondition extends Condition {
        
        public OrCondition(List<Operand> operands)  {
            super(new Operation(252, "or(TESTLIST)", "ExpressionOr"), operands);
        }
    }

    /**
     * An Instruction usually has one or more Operands, although there are some that don't. An
     * Operand is of a particular OperandType and has a Value.
     */
    public class Operand {
        
        public OperandType operandType;

        private Object value;

        /**
         * Constructor for Operand.
         * 
         * @param operandType The OperandType for this Operand.
         * @param value The value for this Operand.
         */
        public Operand(OperandType operandType, Object value)
        {
            this.operandType = operandType;
            this.value = value;
        }

        /**
         * Gets the Operand's value as an int.
         */
        public int asInt() {
            return ((Number)value).intValue();
        }

        /**
         * Gets the Operand's value as a short.
         */
        public short asShort() {
            return ((Number)value).shortValue();
        }

        /**
         * Gets the Operand's value as unsigned byte.
         */
        public int asByte() {
            return (int)(((Number)value).intValue() & 0xFF);
        }

        /**
         * Gets the Operand's value as a signed byte.
         */
        public byte asSByte() {
            return ((Number)value).byteValue();
        }

        /**
         * Gets the Operand's value as a Condition.
         */
        public Condition asCondition() {
            return (Condition)value;
        }

        /**
         * Gets the Operand's value as a List of Conditions.
         */
        @SuppressWarnings("unchecked")
        public List<Condition> asConditions() {
            return (List<Condition>)value;
        }

        @SuppressWarnings("unchecked")
        public List<Integer> asInts() {
            return (List<Integer>)value;
        }
        
        public Object getValue() {
            return value;
        }
    }

    /**
     * The different types of Operand that the AGI Action and Condition instructions can have.
     */
    public enum OperandType {
        VAR,
        NUM,
        FLAG,
        OBJECT,
        WORDLIST,
        VIEW,
        MSGNUM,
        TEST,
        TESTLIST,
        ADDRESS
    }

    /**
     * The Operation class represents an AGI command, e.g. the add operation, or isset 
     * operation. The distinction between the Operation class and the Instruction classes
     * is that an Operation instance holds information about the AGI command, whereas the
     * Instruction classes hold information about an instance of the usage of an AGI 
     * command. So the Operation instances are essentially reference data that is referenced
     * by the Instructions. Multiple Instruction instances can and will refer to the same 
     * Operation.
     */
    public static class Operation {
        
        /**
         * The AGI opcode or bytecode value for this Operation.
         */
        public int opcode;

        /**
         * A format string that describes the name and arguments for this Operation.
         */
        public String format;

        /**
         * The name of this Operation, e.g. set.view
         */
        public String name;

        /**
         * The List of OperandTypes for this Operation.
         */
        public List<OperandType> operandTypes;

        /**
         * The name of the interpreter class that executes this Operation.
         */
        public String executionClass;

        /**
         * Constructor for Operation.
         * 
         * @param opcode The AGI opcode or bytecode value for this Operation.
         * @param format A format string that describes the name and arguments for this Operation.
         * @param executionClass The name of the interpreter class that executes this Operation.
         */
        public Operation(int opcode, String format, String executionClass) {
            this.opcode = opcode;
            this.format = format;
            this.executionClass = executionClass;
            this.operandTypes = new ArrayList<OperandType>();

            // Work out the position of the two brackets in the format string.
            int openBracket = format.indexOf("(");
            int closeBracket = format.indexOf(")");

            // The Name is the bit before the open bracket.
            this.name = format.substring(0, openBracket);

            // If the brackets are not next to each other, the operation has operands.
            if ((closeBracket - openBracket) > 1) {
                String operandsStr = format.substring(openBracket + 1, closeBracket);

                for (String operandTypeStr : operandsStr.split(",")) {
                    OperandType operandType = OperandType.valueOf(operandTypeStr);
                    this.operandTypes.add(operandType);
                }
            }
        }
    }
    
    /**
     * Static array of the AGI TEST Operations.
     */
    private static Operation[] TEST_OPERATIONS = new Operation[]
    {
        null,
        new Operation(1, "equaln(VAR,NUM)", "ExpressionEqual"),
        new Operation(2, "equalv(VAR,VAR)", "ExpressionEqualV"),
        new Operation(3, "lessn(VAR,NUM)", "ExpressionLess"),
        new Operation(4, "lessv(VAR,VAR)", "ExpressionLessV"),
        new Operation(5, "greatern(VAR,NUM)", "ExpressionGreater"),
        new Operation(6, "greaterv(VAR,VAR)", "ExpressionGreaterV"),
        new Operation(7, "isset(FLAG)", "ExpressionIsSet"),
        new Operation(8, "isset.v(VAR)", "ExpressionIsSetV"),
        new Operation(9, "has(OBJECT)", "ExpressionHas"),
        new Operation(10, "obj.in.room(OBJECT,VAR)", "ExpressionObjInRoom"),
        new Operation(11, "posn(OBJECT,NUM,NUM,NUM,NUM)", "ExpressionPosN"),
        new Operation(12, "controller(NUM)", "ExpressionController"),
        new Operation(13, "have.key()", "ExpressionHaveKey"),
        new Operation(14, "said(WORDLIST)", "ExpressionSaid"),
        new Operation(15, "compare.strings(NUM,NUM)", "ExpressionStringCompare"),
        new Operation(16, "obj.in.box(OBJECT,NUM,NUM,NUM,NUM)", "ExpressionObjInBox"),
        new Operation(17, "center.posn(OBJECT,NUM,NUM,NUM,NUM)", "ExpressionCentrePosition"),
        new Operation(18, "right.posn(OBJECT,NUM,NUM,NUM,NUM)", "ExpressionRightPosition")
    };

    /**
     * Adjusts the AGI command definitions to match the given AGI version.
     * 
     * @param version The AGI version to adjust the command definitions to match.
     */
    public static void AdjustCommandsForVersion(String version)
    {
        if (version.equals("2.089")) {
            ACTION_OPERATIONS[134] = new Operation(134, "quit()", "InstructionQuit");
        }
    }

    /**
     * Static array of the AGI ACTION Operations.
     */
    private static Operation[] ACTION_OPERATIONS = new Operation[] {
        new Operation(0, "return()", "InstructionReturn"),
        new Operation(1, "increment(VAR)", "InstructionIncrement"),
        new Operation(2, "decrement(VAR)", "InstructionDecrement"),
        new Operation(3, "assignn(VAR,NUM)", "InstructionAssign"),
        new Operation(4, "assignv(VAR,VAR)", "InstructionAssignV"),
        new Operation(5, "addn(VAR,NUM)", "InstructionAdd"),
        new Operation(6, "addv(VAR,VAR)", "InstructionAddV"),
        new Operation(7, "subn(VAR,NUM)", "InstructionSubstract"),
        new Operation(8, "subv(VAR,VAR)", "InstructionSubstractV"),
        new Operation(9, "lindirectv(VAR,VAR)", "InstructionIndirect"),
        new Operation(10, "rindirect(VAR,VAR)", "InstructionIndirect"),
        new Operation(11, "lindirectn(VAR,NUM)", "InstructionIndirect"),
        new Operation(12, "set(FLAG)", "InstructionSet"),
        new Operation(13, "reset(FLAG)", "InstructionReset"),
        new Operation(14, "toggle(FLAG)", "InstructionToggle"),
        new Operation(15, "set.v(VAR)", "InstructionSet"),
        new Operation(16, "reset.v(VAR)", "InstructionReset"),
        new Operation(17, "toggle.v(VAR)", "InstructionToggle"),
        new Operation(18, "new.room(NUM)", "InstructionNewRoom"),
        new Operation(19, "new.room.f(VAR)", "InstructionNewRoomV"),
        new Operation(20, "load.logics(NUM)", "InstructionLoadLogic"),
        new Operation(21, "load.logics.f(VAR)", "InstructionLoadLogicV"),
        new Operation(22, "call(NUM)", "InstructionCall"),
        new Operation(23, "call.f(VAR)", "InstructionCallV"),
        new Operation(24, "load.pic(VAR)", "InstructionLoadPic"),
        new Operation(25, "draw.pic(VAR)", "InstructionDrawPic"),
        new Operation(26, "show.pic()", "InstructionShowPic"),
        new Operation(27, "discard.pic(VAR)", "InstructionDiscardPic"),
        new Operation(28, "overlay.pic(VAR)", "InstructionOverlayPic"),
        new Operation(29, "show.pri.screen()", "InstructionShowPriScreen"),
        new Operation(30, "load.view(VIEW)", "InstructionLoadView"),
        new Operation(31, "load.view.f(VAR)", "InstructionLoadViewV"),
        new Operation(32, "discard.view(VIEW)", "InstructionDiscardView"),
        new Operation(33, "animate.obj(OBJECT)", "InstructionAnimateObject"),
        new Operation(34, "unanimate.all()", "InstructionUnanimateAll"),
        new Operation(35, "draw(OBJECT)", "InstructionDraw"),
        new Operation(36, "erase(OBJECT)", "InstructionErase"),
        new Operation(37, "position(OBJECT,NUM,NUM)", "InstructionPosition"),
        new Operation(38, "position.f(OBJECT,VAR,VAR)", "InstructionPositionV"),
        new Operation(39, "get.posn(OBJECT,VAR,VAR)", "InstructionGetPosition"),
        new Operation(40, "reposition(OBJECT,VAR,VAR)", "InstructionReposition"),
        new Operation(41, "set.view(OBJECT,VIEW)", "InstructionSetView"),
        new Operation(42, "set.view.f(OBJECT,VAR)", "InstructionSetViewV"),
        new Operation(43, "set.loop(OBJECT,NUM)", "InstructionSetLoop"),
        new Operation(44, "set.loop.f(OBJECT,VAR)", "InstructionSetLoopV"),
        new Operation(45, "fix.loop(OBJECT)", "InstructionFixLoop"),
        new Operation(46, "release.loop(OBJECT)", "InstructionReleaseLoop"),
        new Operation(47, "set.cel(OBJECT,NUM)", "InstructionSetCell"),
        new Operation(48, "set.cel.f(OBJECT,VAR)", "InstructionSetCellV"),
        new Operation(49, "last.cel(OBJECT,VAR)", "InstructionLastCell"),
        new Operation(50, "current.cel(OBJECT,VAR)", "InstructionCurrentCell"),
        new Operation(51, "current.loop(OBJECT,VAR)", "InstructionCurrentLoop"),
        new Operation(52, "current.view(OBJECT,VAR)", "InstructionCurrentView"),
        new Operation(53, "number.of.loops(OBJECT,VAR)", "InstructionLastLoop"),
        new Operation(54, "set.priority(OBJECT,NUM)", "InstructionSetPriority"),
        new Operation(55, "set.priority.f(OBJECT,VAR)", "InstructionSetPriorityV"),
        new Operation(56, "release.priority(OBJECT)", "InstructionReleasePriority"),
        new Operation(57, "get.priority(OBJECT,VAR)", "InstructionGetPriority"),
        new Operation(58, "stop.update(OBJECT)", "InstructionStopUpdate"),
        new Operation(59, "start.update(OBJECT)", "InstructionStartUpdate"),
        new Operation(60, "force.update(OBJECT)", "InstructionForceUpdate"),
        new Operation(61, "ignore.horizon(OBJECT)", "InstructionIgnoreHorizon"),
        new Operation(62, "observe.horizon(OBJECT)", "InstructionObserveHorizon"),
        new Operation(63, "set.horizon(NUM)", "InstructionSetHorizon"),
        new Operation(64, "object.on.water(OBJECT)", "InstructionObjectOnWater"),
        new Operation(65, "object.on.land(OBJECT)", "InstructionObjectOnLand"),
        new Operation(66, "object.on.anything(OBJECT)", "InstructionObjectOnAnything"),
        new Operation(67, "ignore.objs(OBJECT)", "InstructionIgnoreObjects"),
        new Operation(68, "observe.objs(OBJECT)", "InstructionObserveObjects"),
        new Operation(69, "distance(OBJECT,OBJECT,VAR)", "InstructionDistance"),
        new Operation(70, "stop.cycling(OBJECT)", "InstructionStopCycling"),
        new Operation(71, "start.cycling(OBJECT)", "InstructionStartCycling"),
        new Operation(72, "normal.cycle(OBJECT)", "InstructionNormalCycling"),
        new Operation(73, "end.of.loop(OBJECT,FLAG)", "InstructionEndOfLoop"),
        new Operation(74, "reverse.cycle(OBJECT)", "InstructionReverseCycling"),
        new Operation(75, "reverse.loop(OBJECT,FLAG)", "InstructionReverseLoop"),
        new Operation(76, "cycle.time(OBJECT,VAR)", "InstructionCycleTime"),
        new Operation(77, "stop.motion(OBJECT)", "InstructionStopMotion"),
        new Operation(78, "start.motion(OBJECT)", "InstructionStartMotion"),
        new Operation(79, "step.size(OBJECT,VAR)", "InstructionStepSize"),
        new Operation(80, "step.time(OBJECT,VAR)", "InstructionStepTime"),
        new Operation(81, "move.obj(OBJECT,NUM,NUM,NUM,FLAG)", "InstructionMoveObject"),
        new Operation(82, "move.obj.f(OBJECT,VAR,VAR,VAR,FLAG)", "InstructionMoveObjectV"),
        new Operation(83, "follow.ego(OBJECT,NUM,FLAG)", "InstructionFollowEgo"),
        new Operation(84, "wander(OBJECT)", "InstructionWander"),
        new Operation(85, "normal.motion(OBJECT)", "InstructionNormalMotion"),
        new Operation(86, "set.dir(OBJECT,VAR)", "InstructionSetDir"),
        new Operation(87, "get.dir(OBJECT,VAR)", "InstructionGetDir"),
        new Operation(88, "ignore.blocks(OBJECT)", "InstructionIgnoreBlocks"),
        new Operation(89, "observe.blocks(OBJECT)", "InstructionObserveBlocks"),
        new Operation(90, "block(NUM,NUM,NUM,NUM)", "InstructionBlock"),
        new Operation(91, "unblock()", "InstructionUnblock"),
        new Operation(92, "get(OBJECT)", "InstructionGet"),
        new Operation(93, "get.f(VAR)", "InstructionGetV"),
        new Operation(94, "drop(OBJECT)", "InstructionDrop"),
        new Operation(95, "put(OBJECT,VAR)", "InstructionPut"),
        new Operation(96, "put.f(VAR,VAR)", "InstructionPutV"),
        new Operation(97, "get.room.f(VAR,VAR)", "InstructionGetRoom"),
        new Operation(98, "load.sound(NUM)", "InstructionLoadSound"),
        new Operation(99, "sound(NUM,FLAG)", "InstructionPlaySound"),
        new Operation(100, "stop.sound()", "InstructionStopSound"),
        new Operation(101, "print(MSGNUM)", "InstructionPrint"),
        new Operation(102, "print.f(VAR)", "InstructionPrintV"),
        new Operation(103, "display(NUM,NUM,MSGNUM)", "InstructionDisplay"),
        new Operation(104, "display.f(VAR,VAR,VAR)", "InstructionDisplayV"),
        new Operation(105, "clear.lines(NUM,NUM,NUM)", "InstructionClearLine"),
        new Operation(106, "text.screen()", "InstructionTextScreen"),
        new Operation(107, "graphics()", "InstructionGraphics"),
        new Operation(108, "set.cursor.char(MSGNUM)", "InstructionSetCursorChar"),
        new Operation(109, "set.text.attribute(NUM,NUM)", "InstructionSetTextAttributes"),
        new Operation(110, "shake.screen(NUM)", "InstructionShakeScreen"),
        new Operation(111, "configure.screen(NUM,NUM,NUM)", "InstructionConfigureScreen"),
        new Operation(112, "status.line.on()", "InstructionStatusLineOn"),
        new Operation(113, "status.line.off()", "InstructionStatusLineOff"),
        new Operation(114, "set.string(NUM,MSGNUM)", "InstructionSetString"),
        new Operation(115, "get.string(NUM,MSGNUM,NUM,NUM,NUM)", "InstructionGetString"),
        new Operation(116, "word.to.string(NUM,NUM)", "InstructionWordToString"),
        new Operation(117, "parse(NUM)", "InstructionParse"),
        new Operation(118, "get.num(MSGNUM,VAR)", "InstructionGetNum"),
        new Operation(119, "prevent.input()", "InstructionPreventInput"),
        new Operation(120, "accept.input()", "InstructionAcceptInput"),
        new Operation(121, "set.key(NUM,NUM,NUM)", "InstructionSetKey"),
        new Operation(122, "add.to.pic(VIEW,NUM,NUM,NUM,NUM,NUM,NUM)", "InstructionAddToPic"),
        new Operation(123, "add.to.pic.f(VAR,VAR,VAR,VAR,VAR,VAR,VAR)", "InstructionAddToPicV"),
        new Operation(124, "status()", "InstructionStatus"),
        new Operation(125, "save.game()", "InstructionSaveGame"),
        new Operation(126, "restore.game()", "InstructionRestoreGame"),
        new Operation(127, "init.disk()", "InstructionInitDisk"),
        new Operation(128, "restart.game()", "InstructionRestartGame"),
        new Operation(129, "show.obj(VIEW)", "InstructionShowObject"),
        new Operation(130, "random(NUM,NUM,VAR)", "InstructionRandom"),
        new Operation(131, "program.control()", "InstructionProgramControl"),
        new Operation(132, "player.control()", "InstructionPlayerControl"),
        new Operation(133, "obj.status.f(VAR)", "InstructionObjectStatus"),
        new Operation(134, "quit(NUM)", "InstructionQuit"),                          // Remove parameter for AGI v2.001/v2.089
        new Operation(135, "show.mem()", "InstructionShowMem"),
        new Operation(136, "pause()", "InstructionPause"),
        new Operation(137, "echo.line()", "InstructionEchoLine"),
        new Operation(138, "cancel.line()", "InstructionCancelLine"),
        new Operation(139, "init.joy()", "InstructionInitJoystick"),
        new Operation(140, "toggle.monitor()", "InstructionToggleMonitor"),
        new Operation(141, "version()", "InstructionVersion"),
        new Operation(142, "script.size(NUM)", "InstructionSetScriptSize"),
        new Operation(143, "set.game.id(MSGNUM)", "InstructionSetGameID"),           // Command is max.drawn(NUM) for AGI v2.001
        new Operation(144, "log(MSGNUM)", "InstructionLog"),
        new Operation(145, "set.scan.start()", "InstructionSetScanStart"),
        new Operation(146, "reset.scan.start()", "InstructionSetScanStart"),
        new Operation(147, "reposition.to(OBJECT,NUM,NUM)", "InstructionPosition"),
        new Operation(148, "reposition.to.f(OBJECT,VAR,VAR)", "InstructionPositionV"),
        new Operation(149, "trace.on()", "InstructionTraceOn"),
        new Operation(150, "trace.info(NUM,NUM,NUM)", "InstructionTraceInfo"),
        new Operation(151, "print.at(MSGNUM,NUM,NUM,NUM)", "InstructionPrintAt"),
        new Operation(152, "print.at.v(VAR,NUM,NUM,NUM)", "InstructionPrintAtV"),
        new Operation(153, "discard.view.v(VAR)", "InstructionDiscardView"),
        new Operation(154, "clear.text.rect(NUM,NUM,NUM,NUM,NUM)", "InstructionClearTextRect"),
        new Operation(155, "set.upper.left(NUM,NUM)", "InstructionUpperLeft"),
        new Operation(156, "set.menu(MSGNUM)", "InstructionSetMenu"),
        new Operation(157, "set.menu.item(MSGNUM,NUM)", "InstructionSetMenuItem"),
        new Operation(158, "submit.menu()", "InstructionSubmitMenu"),
        new Operation(159, "enable.item(NUM)", "InstructionEnableItem"),
        new Operation(160, "disable.item(NUM)", "InstructionDisableItem"),
        new Operation(161, "menu.input()", "InstructionMenuInput"),
        new Operation(162, "show.obj.v(VAR)", "InstructionShowObject"),
        new Operation(163, "open.dialogue()", "InstructionOpenDialogue"),
        new Operation(164, "close.dialogue()", "InstructionCloseDialogue"),
        new Operation(165, "mul.n(VAR,NUM)", "InstructionMultiply"),
        new Operation(166, "mul.v(VAR,VAR)", "InstructionMultiplyV"),
        new Operation(167, "div.n(VAR,NUM)", "InstructionDivide"),
        new Operation(168, "div.v(VAR,VAR)", "InstructionDivideV"),
        new Operation(169, "close.window()", "InstructionCloseWindow"),
        new Operation(170, "set.simple(NUM)", "InstructionSetSimple"),
        new Operation(171, "push.script()", "InstructionPushScript"),
        new Operation(172, "pop.script()", "InstructionPopScript"),
        new Operation(173, "hold.key()", "InstructionHoldKey"),
        new Operation(174, "set.pri.base(NUM)", "InstructionSetPriorityBase"),
        new Operation(175, "discard.sound(NUM)", "InstructionDiscardSound"),
        new Operation(176, "hide.mouse()", "InstructionHideMouse"),
        new Operation(177, "allow.menu(NUM)", "InstructionAllowMenu"),
        new Operation(178, "show.mouse()", "InstructionShowMouse"),
        new Operation(179, "fence.mouse(NUM,NUM,NUM,NUM)", "InstructionFenceMouse"),
        new Operation(180, "mouse.posn(VAR,VAR)", "InstructionMousePosition"),
        new Operation(181, "release.key()", "InstructionReleaseKey"),
        new Operation(182, "adj.ego.move.to.x.y(NUM,NUM)", "InstructionAdjustEgoMoveToXY"),
        new Operation(254, "goto(ADDRESS)", "InstructionGoto"),
        new Operation(255, "if(TESTLIST,ADDRESS)", "InstructionIf")
    };
}
