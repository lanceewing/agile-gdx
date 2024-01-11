package com.agifans.agile;

/**
 * An abstract class for playing WAV data. The desktop, mobile, and HTML platforms will
 * extend this in their own way. We generally have a lot more control over sound
 * if we ignore the libgdx platform independent audio classes and instead use 
 * platform specific audio techniques. This is particularly the case for HTML. It is 
 * doesn't seem possible to play a WAV file from a byte array and request callback 
 * at the end when using libgdx, but its perfectly possible in JavaScript.
 */
public abstract class WavePlayer {
    
    protected VariableData variableData;

    /**
     * Plays the given WAV file data, and when finished, calls the given 
     * endCallback Runnable.
     * 
     * @param waveData A byte array containing the WAV data to play.
     * @param endFlag The flag to set when the sound ends.
     */
    public abstract void playWaveData(byte[] waveData, int endFlag);
    
    /**
     * Request the WavePlayer implementation to stop playing the WAV.
     * 
     * @param wait @param wait true to wait for the WAV player to finish playing; otherwise false to not wait.
     */
    public abstract void stopPlaying(boolean wait);
    
    /**
     * Resets the state of the WavePlayer, as if it is newly instantiated. This is 
     * intended to be calling in scenarios such as when the room has changed, or 
     * when a saved game has been restored. The platform specific implementations may
     * or may not actually do anything.
     */
    public abstract void reset();
    
    /**
     * Dispose of any audio device objects that were created to support sound 
     * play back. Whether this does anything or not depends on the platform specific
     * implementation.
     */
    public abstract void dispose();
    
    /**
     * In order to support setting the end flag for when a sound stops playing, the 
     * WavePlayer will need to support accepting the VariableData implementation.
     * 
     * @param variableData The VariableData implementation in use by the platform.
     */
    public void setVariableData(VariableData variableData) {
        this.variableData = variableData;
    }
}
