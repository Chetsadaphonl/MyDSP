package com.example.mydsp;

/**
 * Native DSP core. process() accepts interleaved float PCM samples in [-1, 1].
 * This starter does not attach to Android's global audio mixer.
 */
public final class NativeDsp {
    static {
        System.loadLibrary("mydsp");
    }

    private NativeDsp() {}

    public static native void setBand(int index, float gainDb);
    public static native void setPreamp(float gainDb);
    public static native void reset();
    public static native void process(float[] interleavedPcm, int channels, int sampleRate);
}
