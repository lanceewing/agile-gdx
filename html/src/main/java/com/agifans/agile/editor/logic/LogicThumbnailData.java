package com.agifans.agile.editor.logic;

public class LogicThumbnailData {

    private String logicNumber;
    
    public LogicThumbnailData(int logicNumber) {
        this.logicNumber = Integer.toString(logicNumber);
    }
    
    public String getLogicNumber() {
        return logicNumber;
    }
}
