package com.agifans.agile.editor.picture;

public class PictureThumbnailData {

    private String pictureNumber;
    
    public PictureThumbnailData(int pictureNumber) {
        this.pictureNumber = Integer.toString(pictureNumber);
    }
    
    public String getPictureNumber() {
        return pictureNumber;
    }
}
