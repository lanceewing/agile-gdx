package com.agifans.agile.editor.view;

public class ViewThumbnailData {

    private String viewNumber;
    
    public ViewThumbnailData(int viewNumber) {
        this.viewNumber = Integer.toString(viewNumber);
    }
    
    public String getViewNumber() {
        return viewNumber;
    }
}
