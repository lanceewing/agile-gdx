package com.agifans.agile;

/**
 * The core constants and definitions within the AGI system.
 */
public class Defines {

    /* ------------------------ System variables -------------------------- */

    public final static int CURROOM = 0;           /* current.room */

    public final static int PREVROOM = 1;          /* previous.room */

    public final static int EGOEDGE = 2;           /* edge.ego.hit */

    public final static int SCORE = 3;             /* score */

    public final static int OBJHIT = 4;            /* obj.hit.edge */

    public final static int OBJEDGE = 5;           /* edge.obj.hit */

    public final static int EGODIR = 6;            /* ego's direction */

    public final static int MAXSCORE = 7;          /* maximum possible score */

    public final static int MEMLEFT = 8;           /* remaining heap space in pages */

    public final static int UNKNOWN_WORD = 9;      /* number of unknown word */

    public final static int ANIMATION_INT = 10;    /* animation interval */

    public final static int SECONDS = 11;

    public final static int MINUTES = 12;          /* time since game start */

    public final static int HOURS = 13;

    public final static int DAYS = 14;

    public final static int DBL_CLK_DELAY = 15;

    public final static int CURRENT_EGO = 16;

    public final static int ERROR_NUM = 17;

    public final static int ERROR_PARAM = 18;

    public final static int LAST_CHAR = 19;

    public final static int MACHINE_TYPE = 20;

    public final static int PRINT_TIMEOUT = 21;

    public final static int NUM_VOICES = 22;

    public final static int ATTENUATION = 23;

    public final static int INPUTLEN = 24;

    public final static int SELECTED_OBJ = 25;     /* selected object number */

    public final static int MONITOR_TYPE = 26;


    /* ------------------------ System flags ------------------------ */

    public final static int ONWATER = 0;               /* on.water */

    public final static int SEE_EGO = 1;               /* can we see ego? */

    public final static int INPUT = 2;                 /* have.input */

    public final static int HITSPEC = 3;               /* hit.special */

    public final static int HADMATCH = 4;              /* had a word match */

    public final static int INITLOGS = 5;              /* signal to init logics */

    public final static int RESTART = 6;               /* is a restart in progress? */

    public final static int NO_SCRIPT = 7;             /* don't add to the script buffer */

    public final static int DBL_CLK = 8;               /* enable double click on joystick */

    public final static int SOUNDON = 9;               /* state of sound playing */

    public final static int TRACE_ENABLE = 10;         /* to enable tracing */

    public final static int HAS_NOISE = 11;            /* does machine have noise channel */

    public final static int RESTORE = 12;              /* restore game in progress */

    public final static int ENABLE_SELECT = 13;        /* allow selection of objects from inventory screen */

    public final static int ENABLE_MENU = 14;

    public final static int LEAVE_WIN = 15;            /* leave windows on the screen */

    public final static int NO_PRMPT_RSTRT = 16;       /* don't prompt on restart */


    /* ------------------------ Miscellaneous ------------------------ */

    public final static int NUMVARS = 256;             /* number of vars */

    public final static int NUMFLAGS = 256;            /* number of flags */

    public final static int NUMCONTROL = 50;           /* number of controllers */

    public final static int NUMWORDS = 10;             /* maximum # of words recognized in input */

    public final static int NUMANIMATED = 256;         /* maximum # of animated objects */

    public final static int MAXVAR = 255;              /* maximum value for a var */

    public final static int TEXTCOLS = 40;             /* number of columns of text */

    public final static int TEXTLINES = 25;            /* number of lines of text */

    public final static int MAXINPUT = 40;             /* maximum length of user input */

    public final static int DIALOGUE_WIDTH = 35;       /* maximum width of dialog box */

    public final static int NUMSTRINGS = 24;           /* number of user-definable strings */

    public final static int STRLENGTH = 40;            /* maximum length of user strings */

    public final static int GLSIZE = 40;               /* maximum length for GetLine calls, used internally for things like save dialog */

    public final static int PROMPTSTR = 0;             /* string number of prompt */

    public final static int ID_LEN = 7;                /* length of gameID string */

    public final static int MAXDIST = 50;              /* maximum movement distance */

    public final static int MINDIST = 6;               /* minimum movement distance */

    public final static int BACK_MOST_PRIORITY = 4;    /* priority value of back most priority */


    /* ------------------------ Inventory item final staticants --------------------------- */

    public final static int LIMBO = 0;                 /* room number of objects that are gone */

    public final static int CARRYING = 255;            /* room number of objects in ego's posession */


    /* ------------------------ Default status and input row numbers ------------------------ */
    
    public final static int STATUSROW = 21;

    public final static int INPUTROW = 23;


    /* ------------------------ Screen edges ------------------------ */

    public final static int TOP = 1;

    public final static int RIGHT = 2;

    public final static int BOTTOM = 3;

    public final static int LEFT = 4;

    public final static int MINX = 0;

    public final static int MINY = 0;

    public final static int MAXX = 159;

    public final static int MAXY = 167;

    public final static int HORIZON = 36;

}
