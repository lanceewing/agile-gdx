package com.agifans.agile.lwjgl3;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.agifans.agile.WavePlayer;

/**
 * An implementation of the WavePlayer interface that uses the standard Java
 * Sound API to play the sound.
 */
public class DesktopWavePlayer implements WavePlayer {
    
    private Clip audioClip;
    
    private AudioInputStream audioStream;
    
    /**
     * Constructor for DesktopWavePlayer.
     */
    public DesktopWavePlayer() {
        
    }

    @Override
    public void playWaveData(byte[] waveData, Runnable endedCallback) {
        try {
            // NOTE: AGI only supports playing one SOUND at a time, so we don't need
            // to worry about handling multiple Clips.
            audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(waveData));
            audioClip = AudioSystem.getClip();
            audioClip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType().equals(Type.STOP)) {
                        endedCallback.run();
                    }
                }
            });
            audioClip.open(audioStream);
            audioClip.start();
        }
        catch (UnsupportedAudioFileException e) {
            // Shouldn't happen, but if it does, we'll pretend the sound ended.
            endedCallback.run();
        }
        catch (IOException e) {
            // Shouldn't happen, but if it does, we'll pretend the sound ended.
            endedCallback.run();
        }
        catch (LineUnavailableException e) {
            // Shouldn't happen, but if it does, we'll pretend the sound ended.
            endedCallback.run();
        }
    }

    @Override
    public void stopPlaying(boolean wait) {
        if (audioClip != null) {
            // Not sure if it should be stop() or close(), but stop() seems to work okay.
            audioClip.stop();
        }
    }

    @Override
    public boolean isPlaying() {
        return ((audioClip != null) && audioClip.isRunning());
    }

    @Override
    public void reset() {
        dispose();
        audioClip = null;
        audioStream = null;
    }

    @Override
    public void dispose() {
        if (audioClip != null) {
            audioClip.close();
        }
        if (audioStream != null) {
            try {
                audioStream.close();
            }
            catch (IOException e) {
                // Don't think there is much we can do. Maybe it is already closed, in
                // which case we can ignore.
            }
        }
    }
}
