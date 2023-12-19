package com.agifans.agile;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.agilib.Sound;
import com.agifans.agile.agilib.Sound.Note;

/**
 * A class for playing AGI sounds.
 */
public class SoundPlayer {
    
    private static final int SAMPLE_RATE = 44100;

    /**
     * The GameState class holds all of the data and state for the Game currently 
     * being run by the interpreter.
     */
    private GameState state;

    /**
     * A cache of the generated WAVE data for loaded sounds.
     */
    public Map<Integer, byte[]> soundCache;

    /**
     * The number of the Sound resource currently playing, or -1 if none should be playing.
     */
    private int soundNumPlaying;

    /**
     * The WavePlayer that will play the generated WAV file data.
     */
    private WavePlayer wavePlayer;

    private static final short[] dissolveDataV2 = new short[] {
          -2,   -3,   -2,   -1, 0x00, 0x00, 0x01, 0x01, 
        0x01, 0x01, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 
        0x02, 0x02, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03,
        0x03, 0x04, 0x04, 0x04, 0x04, 0x05, 0x05, 0x05, 
        0x05, 0x06, 0x06, 0x06, 0x06, 0x06, 0x07, 0x07, 
        0x07, 0x07, 0x08, 0x08, 0x08, 0x08, 0x09, 0x09, 
        0x09, 0x09, 0x0A, 0x0A, 0x0A, 0x0A, 0x0B, 0x0B, 
        0x0B, 0x0B, 0x0B, 0x0B, 0x0C, 0x0C, 0x0C, 0x0C, 
        0x0C, 0x0C, 0x0D, -100
    };

    private static final short[] dissolveDataV3 = new short[] {
          -2,   -3,   -2,   -1, 0x00, 0x00, 0x00, 0x00, 
        0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x02, 
        0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
        0x02, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03,
        0x03, 0x04, 0x04, 0x04, 0x04, 0x04, 0x05, 0x05, 
        0x05, 0x05, 0x05, 0x06, 0x06, 0x06, 0x06, 0x06,
        0x07, 0x07, 0x07, 0x07, 0x08, 0x08, 0x08, 0x08,
        0x09, 0x09, 0x09, 0x09, 0x0A, 0x0A, 0x0A, 0x0A,
        0x0B, 0x0B, 0x0B, 0x0B, 0x0B, 0x0B, 0x0C, 0x0C, 
        0x0C, 0x0C, 0x0C, 0x0C, 0x0D, -100
    };

    private short[] dissolveData;

    /**
     * Constructor for SoundPlayer.
     * 
     * @param state
     * @param wavePlayer The WavePlayer that will play the generated WAV file data.
     */
    public SoundPlayer(GameState state, WavePlayer wavePlayer) {
        this.state = state;
        this.wavePlayer = wavePlayer;
        this.soundCache = new HashMap<Integer, byte[]>();
        this.soundNumPlaying = -1;
        this.dissolveData = (state.isAGIV3()? dissolveDataV3 : dissolveDataV2);
    }

    /**
     * Loads and generates an AGI Sound, caching it in a ready to play state.
     * 
     * @param sound The AGI sound to load.
     */
    public void loadSound(Sound sound) {
        Note[] voiceCurrentNote = new Note[4];
        boolean[] voicePlaying = new boolean[] { true, true, true, true };
        int[] voiceSampleCount = new int[4];
        int[] voiceNoteNum = new int[4];
        int[] voiceDissolveCount = new int[4];
        int durationUnitCount = 0;

        // A single note duration unit is 1/60th of a second
        int samplesPerDurationUnit = SAMPLE_RATE / 60;
        ByteArrayOutputStream sampleStream = new ByteArrayOutputStream();

        // Create a new PSG for each sound, to guarantee a clean state.
        SN76496 psg = new SN76496();

        // Start by converting the Notes into samples.
        while (voicePlaying[0] || voicePlaying[1] || voicePlaying[2] || voicePlaying[3]) {
            for (int voiceNum = 0; voiceNum < 4; voiceNum++) {
                if (voicePlaying[voiceNum]) {
                    if (voiceSampleCount[voiceNum]-- <= 0) {
                        if (voiceNoteNum[voiceNum] < sound.notes.get(voiceNum).size()) {
                            voiceCurrentNote[voiceNum] = sound.notes.get(voiceNum).get(voiceNoteNum[voiceNum]++);
                            byte[] psgBytes = voiceCurrentNote[voiceNum].rawData;
                            psg.write(psgBytes[3] & 0xFF);
                            psg.write(psgBytes[2] & 0xFF);
                            psg.write(psgBytes[4] & 0xFF);
                            voiceSampleCount[voiceNum] = voiceCurrentNote[voiceNum].duration * samplesPerDurationUnit;
                            voiceDissolveCount[voiceNum] = 0;
                        }
                        else {
                            voicePlaying[voiceNum] = false;
                            psg.setVolByNumber(voiceNum, 0x0F);
                        }
                    }
                    if ((durationUnitCount == 0) && (voicePlaying[voiceNum])) {
                        voiceDissolveCount[voiceNum] = updateVolume(psg, voiceCurrentNote[voiceNum].origVolume, voiceNum, voiceDissolveCount[voiceNum]);
                    }
                }
            }

            // This count hits zero 60 times a second. It counts samples from 0 to 734 (i.e. (44100 / 60) - 1).
            durationUnitCount = ((durationUnitCount + 1) % samplesPerDurationUnit);

            // Use the SN76496 PSG emulation to generate the sample data.
            short sample = (short)(psg.render());
            sampleStream.write(sample & 0xFF);
            sampleStream.write((sample >> 8) & 0xFF);
            sampleStream.write(sample & 0xFF);
            sampleStream.write((sample >> 8) & 0xFF);
        }

        // Use the samples to create a Wave file. These can be several MB in size (e.g. 5MB, 8MB, 10MB)
        byte[] waveData = createWave(sampleStream.toByteArray());
        
        // Cache for use when the sound is played. This reduces overhead of generating WAV on every play.
        this.soundCache.put(sound.index, waveData);
    }

    /**
     * Creates a WAVE file from the given sample data by pre-pending the 
     * standard WAV file format header to the start.
     *
     * @param sampleData The sample data to create the WAVE file from.
     * 
     * @return byte array containing the WAV file data.
     */
    private byte[] createWave(byte[] sampleData) {
        // Create WAVE header
        int headerLen = 44;
        int l1 = (sampleData.length + headerLen) - 8;   // Total size of file minus 8.
        int l2 = sampleData.length;
        byte[] wave = new byte[headerLen + sampleData.length];
        byte[] header = new byte[] {
            82, 73, 70, 70,   // RIFF
            (byte)(l1 & 255), (byte)((l1 >> 8) & 255), (byte)((l1 >> 16) & 255), (byte)((l1 >> 24) & 255),
            87, 65, 86, 69,      // WAVE
            102, 109, 116, 32,   // fmt  (chunk ID)
            16, 0, 0, 0,         // size (chunk size)
            1, 0,                // audio format (PCM = 1, i.e. Linear quantization)
            2, 0,                // number of channels
            68, (byte)172, 0, 0, // sample rate (samples per second), i.e. 44100
            16, (byte)177, 2, 0, // byte rate (average bytes per second, == SampleRate * NumChannels * BitsPerSample/8)
            4, 0,                // block align (== NumChannels * BitsPerSample/8)
            16, 0,               // bits per sample (i.e 16 bits per sample)
            100, 97, 116, 97,    // data (chunk ID)
            (byte)(l2 & 255), (byte)((l2 >> 8) & 255), (byte)((l2 >> 16) & 255), (byte)((l2 >> 24) & 255)
        };
        
        System.arraycopy(header, 0, wave, 0, headerLen);
        System.arraycopy(sampleData, 0, wave, headerLen, sampleData.length);

        // Return the WAVE formatted typed array
        return wave;
    }
    
    /**
     * Updates the volume of the given channel, by applying the dissolve data and master volume to the 
     * given base volume and then sets that in the SN76496 PSG. The noise channel does not apply the
     * dissolve data, so skips that bit.
     * 
     * @param psg The SN76496 PSG to set the calculated volume in.
     * @param baseVolume The base volume to apply the dissolve data and master volume to.
     * @param channel The channel to update the volume for.
     * @param dissolveCount The current dissolve count value for the note being played by the given channel.
     * 
     * @return The new dissolve count value for the channel.
     */
    private int updateVolume(SN76496 psg, int baseVolume, int channel, int dissolveCount) {
        int volume = baseVolume;

        if (volume != 0x0F) {
            int dissolveValue = (dissolveData[dissolveCount] == -100 ? dissolveData[dissolveCount - 1] : dissolveData[dissolveCount++]);

            // Add master volume and dissolve value to current channel volume. Noise channel doesn't dissolve.
            if (channel < 3) volume += dissolveValue;
            
            volume += this.state.vars[Defines.ATTENUATION];

            if (volume < 0) volume = 0;
            if (volume > 0x0F) volume = 0x0F;
            if (volume < 8) volume += 2;

            // Apply calculated volume to PSG channel.
            psg.setVolByNumber(channel, volume);
        }

        return dissolveCount;
    }
    
    /**
     * Plays the given AGI Sound.
     * 
     * @param sound The AGI Sound to play.
     * @param endFlag The flag to set when the sound ends.
     */
    public void playSound(Sound sound, int endFlag) {
        // Stop any currently playing sound. Will set the end flag for the previous sound.
        stopSound();

        // Set the starting state of the sound end flag to false.
        state.flags[endFlag] = false;
        
        // Get WAV data from the cache.
        byte[] waveData = this.soundCache.get(sound.index);
        if (waveData != null) {
            // Now play the Wave file.
            if (this.state.flags[Defines.SOUNDON]) {
                soundNumPlaying = sound.index;
                wavePlayer.playWaveData(waveData, () -> {
                    // This is run when the WAV data finishes playing.
                    soundNumPlaying = -1;
                    state.flags[endFlag] = true;
                });
            }
            else {
               // If sound is not on, then it ends immediately.
               soundNumPlaying = -1;
               state.flags[endFlag] = true;
            }
        }
    }
    
    /**
     * Resets the internal state of the SoundPlayer.
     */
    public void reset() {
        stopSound();
        soundCache.clear();
        wavePlayer.reset();
    }

    /**
     * Fully shuts down the SoundPlayer. Only intended for when AGILE is closing down.
     */
    public void shutdown() {
        reset();
        wavePlayer.dispose();
    }

    /**
     * Stops the currently playing sound. This version of the method will always wait
     * for the WAV player to finish playing before returning.
     */
    public void stopSound() {
        stopSound(true);
    }
    
    /**
     * Stops the currently playing sound.
     * 
     * @param wait true to wait for the WAV player to finish playing; otherwise false to not wait.
     */
    public void stopSound(boolean wait) {
        if (soundNumPlaying >= 0) {
            // Store that we're now not playing a sound.
            soundNumPlaying = -1;
            
            // Ask WAV player to stop playing. The wait parameter tells the WAV 
            // player whether or not to wait until it has finished playing.
            wavePlayer.stopPlaying(wait);
        }
    }
    
    /**
     * SN76496 is the audio chip used in the IBM PC JR and therefore what the original AGI sound format was designed for.
     */
    public static class SN76496 {
        
        private static final float IBM_PCJR_CLOCK = 3579545f;

        private static float[] volumeTable = new float[] {
            8191.5f,
            6506.73973474395f,
            5168.4870873095f,
            4105.4752242578f,
            3261.09488758897f,
            2590.37974532693f,
            2057.61177037107f,
            1634.41912530676f,
            1298.26525860452f,
            1031.24875107119f,
            819.15f,
            650.673973474395f,
            516.84870873095f,
            410.54752242578f,
            326.109488758897f,
            0.0f
        };

        private int[] channelVolume = new int[] { 15, 15, 15, 15 };
        private int[] channelCounterReload = new int[4];
        private int[] channelCounter = new int[4];
        private int[] channelOutput = new int[4];
        private int lfsr;
        private int latchedChannel;
        private boolean updateVolume;
        private float ticksPerSample;
        private float ticksCount;

        public SN76496() {
            ticksPerSample = IBM_PCJR_CLOCK / 16 / SAMPLE_RATE;
            ticksCount = ticksPerSample;
            latchedChannel = 0;
            updateVolume = false;
            lfsr = 0x4000;
        }

        public void setVolByNumber(int channel, int volume) {
            channelVolume[channel] = (int)(volume & 0x0F);
        }

        public int getVolByNumber(int channel) {
            return (channelVolume[channel] & 0x0F);
        }

        public void write(int data) {
            /*
             * A tone is produced on a voice by passing the sound chip a 3-bit register address 
             * and then a 10-bit frequency divisor. The register address specifies which voice 
             * the tone will be produced on. 
             * 
             * The actual frequency produced is the 10-bit frequency divisor given by F0 to F9
             * divided into 1/32 of the system clock frequency (3.579 MHz) which turns out to be 
             * 111,860 Hz. Keeping all this in mind, the following is the formula for calculating
             * the frequency:
             * 
             *  f = 111860 / (((Byte2 & 0x3F) << 4) + (Byte1 & 0x0F))
             */
            int counterReloadValue;

            if ((data & 0x80) != 0) {
                // First Byte
                // 7  6  5  4  3  2  1  0
                // 1  .  .  .  .  .  .  .      Identifies first byte (command byte)
                // .  R0 R1 .  .  .  .  .      Voice number (i.e. channel)
                // .  .  .  R2 .  .  .  .      1 = Update attenuation, 0 = Frequency count
                // .  .  .  .  A0 A1 A2 A3     4-bit attenuation value.
                // .  .  .  .  F6 F7 F8 F9     4 of 10 - bits in frequency count.
                latchedChannel = (data >> 5) & 0x03;
                counterReloadValue = (int)((channelCounterReload[latchedChannel] & 0xfff0) | (data & 0x0F));
                updateVolume = ((data & 0x10) != 0) ? true : false;
            }
            else {
                // Second Byte - Frequency count only
                // 7  6  5  4  3  2  1  0
                // 0  .  .  .  .  .  .  .      Identifies second byte (completing byte for frequency count)
                // .  X  .  .  .  .  .  .      Unused, ignored.
                // .  .  F0 F1 F2 F3 F4 F5     6 of 10 - bits in frequency count.
                counterReloadValue = (int)((channelCounterReload[latchedChannel] & 0x000F) | ((data & 0x3F) << 4));
            }

            if (updateVolume) {
                // Volume latched. Update attenuation for latched channel.
                channelVolume[latchedChannel] = (data & 0x0F);
            }
            else {
                // Data latched. Update counter reload register for channel.
                channelCounterReload[latchedChannel] = counterReloadValue;

                // If it is for the noise control register, then set LFSR back to starting value.
                if (latchedChannel == 3) lfsr = 0x4000;
            }
        }

        private void updateToneChannel(int channel) {
            // If the tone counter reload register is 0, then skip update.
            if (channelCounterReload[channel] == 0) return;

            // Note: For some reason SQ2 intro, in docking scene, is quite sensitive to how this is decremented and tested.

            // Decrement channel counter. If zero, then toggle output and reload from
            // the tone counter reload register.
            if (--channelCounter[channel] <= 0) {
                channelCounter[channel] = channelCounterReload[channel];
                channelOutput[channel] ^= 1;
            }
        }

        public float render() {
            while (ticksCount > 0) {
                updateToneChannel(0);
                updateToneChannel(1);
                updateToneChannel(2);

                channelCounter[3] -= 1;
                if (channelCounter[3] < 0) {
                    // Reload noise counter.
                    if ((channelCounterReload[3] & 0x03) < 3) {
                        channelCounter[3] = (0x20 << (channelCounterReload[3] & 3));
                    }
                    else {
                        // In this mode, the counter reload value comes from tone register 2.
                        channelCounter[3] = channelCounterReload[2];
                    }

                    int feedback = ((channelCounterReload[3] & 0x04) == 0x04) ?
                        // White noise. Taps bit 0 and bit 1 of the LFSR as feedback, with XOR.
                        ((lfsr & 0x0001) ^ ((lfsr & 0x0002) >> 1)) :
                        // Periodic. Taps bit 0 for the feedback.
                        (lfsr & 0x0001);

                    // LFSR is shifted every time the counter times out. SR is 15-bit. Feedback added to top bit.
                    lfsr = (lfsr >> 1) | (feedback << 14);
                    channelOutput[3] = (int)(lfsr & 1);
                }

                ticksCount -= 1;
            }

            ticksCount += ticksPerSample;

            return (float)((volumeTable[channelVolume[0] & 0x0F] * ((channelOutput[0] - 0.5) * 2)) +
                           (volumeTable[channelVolume[1] & 0x0F] * ((channelOutput[1] - 0.5) * 2)) +
                           (volumeTable[channelVolume[2] & 0x0F] * ((channelOutput[2] - 0.5) * 2)) +
                           (volumeTable[channelVolume[3] & 0x0F] * ((channelOutput[3] - 0.5) * 2)));
        }
    }
}
