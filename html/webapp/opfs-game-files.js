/**
 * Constructor for OPFSGameFiles.
 */
var OPFSGameFiles = function() {

    //--------------------------------------------------------------------------
    // Private members
    //--------------------------------------------------------------------------

    let opfsRoot;
    let gameFilesDir;

    //--------------------------------------------------------------------------
    // Private methods
    //--------------------------------------------------------------------------

    const initGameFilesDirectory = async function() {
		// Get and store a handle to the OPFS root dir for the AGILE website.
        opfsRoot = await navigator.storage.getDirectory();
        
        // Get and store a handle to the "Game Files" sub directory.
        gameFilesDir = await opfsRoot.getDirectoryHandle('Game Files', {create: true});
    };

    const writeGameFilesData = async function(gameDirectoryName, gameDataUint8Array) {
		// Create an OPFS sub-directory and data file for this game.
		const gameDir = await gameFilesDir.getDirectoryHandle(gameDirectoryName, {create: true});
		const gameDataFileHandle = await gameDir.getFileHandle('GAMEFILES.DAT', {create: true});
		
		// Open a writable stream for the file.
		const gameDataFileStream = await gameDataFileHandle.createWritable();
		
		// Write the game data out and then close.
		await gameDataFileStream.write(gameDataUint8Array);
		await gameDataFileStream.close();
	};
	
	const readGameFilesData = async function(gameDirectoryName, resultsHandler) {
		// Get handle to the game's data file.
		const gameDir = await gameFilesDir.getDirectoryHandle(gameDirectoryName, {create: true});
		const gameDataFileHandle = await gameDir.getFileHandle('GAMEFILES.DAT', {create: true});
		const gameDataFile = await gameDataFileHandle.getFile();
		
		// Get the file content as a Uint8Array.
		const gameDataArrayBuffer = await gameDataFile.arrayBuffer();
		const gameDataUint8Array = new Uint8Array(gameDataArrayBuffer);
		
		// Call the results handler with the data array.
		resultsHandler(gameDataUint8Array);
	};

    //--------------------------------------------------------------------------
    // Initialization
    //--------------------------------------------------------------------------

    this.init = function() {
        // Call the async function to perform the set up..
        initGameFilesDirectory();
    };

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    this.readGameFilesData = function(gameDirectoryName, resultsHandler) {
		// Delegate to the async private method and return immediately.
		readGameFilesData(gameDirectoryName, resultsHandler);
    };
    
    this.writeGameFilesData = function(gameDirectoryName, gameDataUint8Array) {
		// Delegate to the async private method and return immediately.
        writeGameFilesData(gameDirectoryName, gameDataUint8Array);
    };
};