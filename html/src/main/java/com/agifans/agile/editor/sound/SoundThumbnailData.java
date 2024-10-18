package com.agifans.agile.editor.sound;

public class SoundThumbnailData {
    
    private String soundNumber;
    
    public SoundThumbnailData(int soundNumber) {
        this.soundNumber = Integer.toString(soundNumber);
    }
    
    public String getSoundNumber() {
        return soundNumber;
    }
}
