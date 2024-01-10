package com.agifans.agile;

/**
 * An Interface for playing WAV data. The desktop, mobile, and HTML platforms will
 * implement this in their own way. We generally have a lot more control over sound
 * if we ignore the libgdx platform independent audio classes and instead use 
 * platform specific audio techniques. This is particularly the case for HTML. It is 
 * doesn't seem possible to play a WAV file from a byte array and request callback 
 * at the end when using libgdx, but its perfectly possible in JavaScript.
 */
public interface WavePlayer {

    /**
     * Plays the given WAV file data, and when finished, calls the given 
     * endCallback Runnable.
     * 
     * @param waveData A byte array containing the WAV data to play.
     * @param endedCallback The callback Runnable to run when finished.
     */
    void playWaveData(byte[] waveData, Runnable endedCallback);
    
    /**
     * Request the WavePlayer implementation to stop playing the WAV.
     * 
     * @param wait @param wait true to wait for the WAV player to finish playing; otherwise false to not wait.
     */
    void stopPlaying(boolean wait);
    
    /**
     * Resets the state of the WavePlayer, as if it is newly instantiated. This is 
     * intended to be calling in scenarios such as when the room has changed, or 
     * when a saved game has been restored. The platform specific implementations may
     * or may not actually do anything.
     */
    void reset();
    
    /**
     * Dispose of any audio device objects that were created to support sound 
     * play back. Whether this does anything or not depends on the platform specific
     * implementation.
     */
    void dispose();
}
