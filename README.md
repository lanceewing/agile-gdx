![](html/webapp/agile_title.png)
# AGILE
**AGILE** is an AGI interpreter whose primary purpose is to run Sierra AGI games on the web! - Supports mobile as well.

https://agi.sierra.games

Development on this web version of AGILE began in November 2023, in hopes that it would be fully complete by the 10th May 1984, to be a tribute to, and to coincide with, the 40th anniversary of the release of King's Quest, the world's first animated graphic adventure game! 

AGI was the name of the [Adventure Game Interpreter](https://en.wikipedia.org/wiki/Adventure_Game_Interpreter) written by [Sierra On-Line](https://en.wikipedia.org/wiki/Sierra_Entertainment) to run the 3D animated adventure games that they released in the 1980s (1984-1989), which included King's Quest 1/2/3/4, Space Quest 1/2, Police Quest, Leisure Suit Larry, Manhunter 1/2, Gold Rush, Donald Duck's Playground, Black Cauldron, and Mixed-Up Mother Goose. There are also over 100 fan-made games and demos that run on the same AGI interpreter system, mostly written in the late 90s and early 2000s by Sierra On-Line fans.

## Features
- Intuitive, familiar, mobile-like UI, with game import and selection screens. Swipe/click to the right:
  
![](img/title_page_web_desktop.jpg)           |![](img/games_page_web_desktop.jpg) 
:-------------------------:|:-------------------------:

- Supports importing the original IBM PC AGI v2 & v3 games, from either a folder or ZIP file.
- Web version comes with over 100 fan-made games and demos already pre-packaged!
- IBM PCJR 4-channel sound, using SN76496 emulation, the sound chip used in the PCJR.
- Game detection of most known versions of Sierra's original IBM PC AGI games, and most AGI fan-made games.
- Saved games that are stored in the browsers OPFS storage.
- Full screen mode. Use the full screen icon when a game is running to toggle this.
- Show Priority screen. Use <kbd>F12</kbd> to show, and ENTER/SPACE to exit.
- Virtual keyboard for use by touch screen devices, such as mobile and tablets.
- Virtual joystick as an alternative to using the arrow keys, also intended for touch screen devices.

Most of the original Sierra On-Line games are still available for purchase online, so for legal reasons, AGILE does not come prepackaged with those games. You must have your own copy and use the import feature to load the game into AGILE. It supports importing from both a folder containing the game, or a ZIP file containing the game. After it has been imported, it will remain in your browser's storage so that you won't have to import it again.

To purchase the original games online, check out [gog.com](https://www.gog.com) and [Steam](https://store.steampowered.com).

## Requirements
AGILE should run on most modern web browsers. It does, however, rely on some browser APIs that are relatively recent, such as the [SharedArrayBuffer](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/SharedArrayBuffer) and [Origin Private File System (OPFS)](https://developer.mozilla.org/en-US/docs/Web/API/File_System_API/Origin_private_file_system). It has been tested on Chrome, Edge, Firefox and Safari, both the desktop and mobile versions, and has been confirmed to work. If it doesn't work for you, then check to make sure that you have updated your browser to the latest version. If it still doesn't work, please create an issue under the [Issues](https://github.com/lanceewing/agile-gdx/issues) section above with the details of your device and browser version.

The UI of AGILE has been designed primarily with mobile devices in mind. Give it a try on your Android, iPhone or iPad! Remember that in order to play the original Sierra games, you'll need your own copy of the game files on your device.

## How to run games
Start by going to https://agi.sierra.games. This will load the AGILE title screen. There is a small question mark icon in the top right that pops up a dialog with a brief description. It mentions that in order to start playing games, simply swipe or click to the right. The screen works in a very similar way to the user interface of a mobile device. If you are accessing the website on a touch screen device, then you can swipe to the right to get to the next page. If you are on desktop, you can use the right arrow key, or drag/fling with your mouse, or click on the small right arrow at the bottom of the screen.

The first page to the right has thumbnails for the original Sierra On-Line games. Notice that they are all faded out. This is to indicate that they are not imported yet. If you click on one of these, it will open a dialog telling you that you must import your own copy of the game for legal reasons. It also asks you if you would like to import from a ZIP file or a folder. After completing the import process, the thumbnail for the game will fully show, rather than being faded out. The game is now imported into the OPFS storage in your browser. If you click on the thumbnail again, it will run the game.

## Game Screenshots

![](img/kq1_web_desktop.jpg)           |![](img/kq2_web_desktop.jpg)           |![](img/kq3_web_desktop.jpg)
:-------------------------:|:-------------------------:|:-------------------------:
![](img/kq4_web_desktop.jpg)           |![](img/sq1_web_desktop.jpg)           |![](img/sq2_web_desktop.jpg)
![](img/pq_web_desktop.jpg)           |![](img/lsl_web_desktop.jpg)           |![](img/gr_web_desktop.jpg)
![](img/mh1_web_desktop.jpg)           |![](img/mh2_web_desktop.jpg)           |![](img/mumg_web_desktop.jpg)
![](img/ddp_web_desktop.jpg)           |![](img/bc_web_desktop.jpg)           |![](img/ruby_web_desktop.jpg)

## Implementation
The web version of AGILE (agile-gdx) started as a port of my C# AGILE Sierra AGI interpreter project to Java, using the libGDX cross-platform development framework, targeting Desktop, Android and HTML5/GWT. The AGILE interpreter code itself is a straight conversion of the C# code to Java, but for the "AGI Library" part, it is instead using a stripped down version of the JAGI project, which was already written in Java and already had code for loading AGI resources. JAGI was originally written by a mysterious author known as Dr Zoltan and was further extended by myself (Lance Ewing) and Mark Yu.

The reason why the libgdx framework was chosen (and why the JAGI bit is stripped down to the bare minimum), was to get it working as a web app, by primarily targetting the GWT/HTML libgdx platform. GWT (i.e. the Google Web Toolkit) is used by libgdx to transpile the Java code into JavaScript, thus the reason why it is written mostly in Java but is able to run on the web.

JavaScript is by default single threaded, which isn't compatible with how AGI blocks waiting for input in some scenarios. To address that in the C# version, and also in the Desktop platform of agile-gdx, a background Thread is created to run the actual Interpreter code, so that the UI thread is not blocked. To achieve the same for HTML5, a web worker was needed, as it allows code to be run outside of the browser's main UI thread. Unfortunately, libgdx and GWT did not provide direct access to that, but by using a project called gwt-webworker written by Manfred Trammel, I was able to get libgdx to support running the code in a web worker.

For details of the original C# version, see here: https://github.com/lanceewing/agile

## Credits and Acknowledgements
This project would not have been possible without the following projects and their authors:

- [libgdx](https://libgdx.com/): The cross-platform game development framework.
- [gdx-liftoff](https://github.com/libgdx/gdx-liftoff): Written by Tommy Ettinger. Used to generate the initial libgdx project boilerplate.
- [JAGI](https://github.com/lanceewing/jagi): Originally written by the mysterious Dr Zoltan (real name not known). Extended by Lance Ewing and Mark Yu.
- [gwt-webworker](https://gitlab.com/ManfredTremmel/gwt-webworker): Written by Manfred Trammel. The key to running libgdx gwt code in a web worker.
- [gwt-jszip](https://github.com/ainslec/GWTJSZip): Originally written by Aki Miyazaki, extended by Chris Ainsley.
- [jszip](https://github.com/Stuk/jszip): Written by Stuart Knightley. Used by AGILE to unzip imported games.
- [GWT](https://www.gwtproject.org): Google Web Toolkit, used by libgdx to transpile the AGILE Java code to JavaScript.
- [PieMenu](https://github.com/payne911/PieMenu): Written by Jérémi Grenier-Berthiaume. Used by AGILE to display the radial menu.
- [ringbuf.js](https://github.com/padenot/ringbuf.js/blob/main/js/ringbuf.js): Written by Paul Adenot. Used for the keyboard event queue in AGILE.

In addition to the above, I would also like to acknowledge the community of [sciprogramming.com](https://sciprogramming.com/) for their testing efforts, encouragement and helpful suggestions for improvement, including doomlazer, Charles, AGKorson, Collector, Kawa, russdanner and pmkelly.

## Fan-made Game Credits
As mentioned above, AGILE comes pre-packaged with over 100 fan-made AGI games and demos. For those games that do not have in-game credits, please refer to the following web page to see who the authors were:

http://agiwiki.sierrahelp.com/index.php/Fan_AGI_Release_List_(Sortable)
