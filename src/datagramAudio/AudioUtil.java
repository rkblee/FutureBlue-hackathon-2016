package datagramAudio;

import javax.sound.sampled.AudioFormat;

public class AudioUtil {
    public static AudioFormat getAudioFormat() {
        float sampleRate = 48000.0F;
        int sampleSizeBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;

        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }

}
