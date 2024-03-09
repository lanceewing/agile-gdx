package com.agifans.agile.ui;

/**
 * Enum representing the different ways that AGILE can import an AGI game.
 */
public enum ImportType {

    DIR("Local folder containing the AGI game files"),
    ZIP("Local ZIP file containing the AGI game files");
    
    private String description;
    
    ImportType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ImportType getImportTypeByDescription(String description) {
        for (ImportType importType : values()) {
            if (importType.description.equals(description)) {
                return importType;
            }
        }
        return null;
    }
    
    public static String[] getDescriptions() {
        String[] descriptions = new String[ImportType.values().length];
        for (int i=0; i < values().length; i++) {
            descriptions[i] = values()[i].description;
        }
        return descriptions;
    }
}
