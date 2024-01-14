package com.agifans.agile.config;

public class AppConfigItem {

  private String name = "";
 
  private String displayName;
  
  private String filePath;
  
  private String fileType;
  
  private String iconPath;
  
  private FileLocation fileLocation = FileLocation.INTERNAL;
  
  private String status = "WORKING";

  public enum FileLocation {
    INTERNAL, EXTERNAL, ABSOLUTE, CLASSPATH, LOCAL
  };
  
  /**
   * Constructor for AppConfigItem.
   */
  public AppConfigItem() {
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
      int numOfSpaces = name.length() - name.replace(" ", "").length();
      displayName = name.replace(" ", "\n");
      if (numOfSpaces == 0) {
        displayName = displayName + "\n";
      }
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

  /**
   * @return the iconPath
   */
  public String getIconPath() {
    return iconPath;
  }

  /**
   * @param iconPath the iconPath to set
   */
  public void setIconPath(String iconPath) {
    this.iconPath = iconPath;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }
}
