package com.agifans.agile.config;

public class AppConfigItem {

    private String gameId;

    private String name = "";

    private String displayName;

    private String filePath;

    private String fileType;

    private FileLocation fileLocation = FileLocation.INTERNAL;

    public enum FileLocation {
        INTERNAL, EXTERNAL, ABSOLUTE, CLASSPATH, LOCAL, OPFS
    };

    /**
     * Constructor for AppConfigItem.
     */
    public AppConfigItem() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * @return the fileLocation
     */
    public FileLocation getFileLocation() {
        return fileLocation;
    }

    /**
     * @param fileLocation the fileLocation to set
     */
    public void setFileLocation(FileLocation fileLocation) {
        this.fileLocation = fileLocation;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        if ((displayName == null) && (name != null)) {
            // Word wrap default to 20 chars.
            StringBuilder displayNameBuilder = new StringBuilder();
            StringBuilder currentLine = new StringBuilder();
            String[] words = name.split(" ");
            for (String word : words) {
                if ((currentLine.length() + word.length() + 1) < 20) {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                } else {
                    displayNameBuilder.append(currentLine.toString());
                    displayNameBuilder.append("\n");
                    currentLine.setLength(0);
                    currentLine.append(word);
                }
            }
            if (currentLine.length() > 0) {
                displayNameBuilder.append(currentLine.toString());
            }
            displayName = displayNameBuilder.toString();
        }
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the fileType
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * @param fileType the fileType to set
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
