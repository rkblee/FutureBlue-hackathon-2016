package datagramAudio;

import javax.sound.sampled.AudioFormat;

public class AudioUtil {
    public static AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleSizeBits = 8;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }

}
