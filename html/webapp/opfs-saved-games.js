/**
 * Constructor for OPFSSavedGames.
 */
var OPFSSavedGames = function() {

    //--------------------------------------------------------------------------
    // Private members
    //--------------------------------------------------------------------------

    let opfsRoot;
    let savedGamesDir;
    let gameDirectory;
    let savedGameSyncAccessHandles = [];

    //--------------------------------------------------------------------------
    // Private methods
    //--------------------------------------------------------------------------

    const initSyncAccessHandles = async function(gameId) {
		// Get and store a handle to the OPFS root dir for the AGILE website.
		opfsRoot = await navigator.storage.getDirectory();
		
		// Get and store a handle to the "Saved Games" sub directory.
		savedGamesDir = await opfsRoot.getDirectoryHandle('Saved Games', {created: true});
		
		// Get and store a handle to this game's sub-directory.
		gameDirectory = await savedGamesDir.getDirectoryHandle(gameId, {create: true});
		
		// Max number of saved games in AGI is 12. To make things easier, we 
		// create empty files up front if they don't already exist, and store all 12
		// sync access file handles for quick synchronous access when needed.
		for (let gameNum=0; gameNum<12; gameNum++) {
			initSyncAccessHandle(gameId, gameNum);
		}
	};
	
	const initSyncAccessHandle = async function(gameId, gameNum) {
		const fileName = `${gameId}SG.${gameNum}`;
	    const fileHandle = await gameDirectory.getFileHandle(fileName, {create: true});
		const syncAccessHandle = await fileHandle.createSyncAccessHandle();
		savedGameSyncAccessHandles[gameNum] = syncAccessHandle;
	};

    //--------------------------------------------------------------------------
    // Initialization
    //--------------------------------------------------------------------------

    this.init = function(gameId) {
		this.gameId = gameId;
		
		// Call the async function to perform the set up for this game ID. In theory
		// it should be done by the time the saved game system is needed.
		initSyncAccessHandles(gameId);
	};

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    this.readSavedGameData = function(gameNum) {
		// Get the cached synchronous access handle.
		let syncAccessHandle = savedGameSyncAccessHandles[gameNum];
		let fileSize = syncAccessHandle.getSize();
		
		if (fileSize == 0) {
			// If we know up front that the file is empty, then return empty array.
			return new Int8Array(0);
		}
		else {
			// Otherwise read the data from the file.
			let savedGameArray = new Int8Array(new ArrayBuffer(fileSize));
			syncAccessHandle.read(savedGameArray, { at: 0 });
			return savedGameArray;
		}
	};
	
	this.writeSavedGameData = function(gameNum, savedGameData) {
		// Get the cached synchronous access handle.
		let syncAccessHandle = savedGameSyncAccessHandles[gameNum];
		
		// Write out the ArrayBuffer and then immediately flush.
		syncAccessHandle.write(savedGameData);
		syncAccessHandle.flush();
		
		// Close and reopen to get a fresh file handle. Might be overkill but seems better.
		// TODO: Test if we don't need the below, by saving then restoring, over and over.
		//fileHandle.close();
		//initSyncAccessFileHandle(this.gameId, gameNum);
	};
};