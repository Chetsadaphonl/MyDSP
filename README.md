# MyDSP Engine (starter)

Java Android UI + C++17 JNI DSP core. Minimum Android version: Android 10 (API 29).

## Current implementation
- Five peaking-EQ bands: 60 Hz, 250 Hz, 1 kHz, 4 kHz, 12 kHz.
- Per-band gain range: -12 dB to +12 dB.
- Native biquad processing for interleaved float PCM.
- JNI API: `setBand`, `setPreamp`, `reset`, `process`.

## Important: system-wide audio is not connected yet
This is an actual native DSP processing core, but the starter UI only changes its parameters. The `process(float[] ...)` method processes PCM buffers explicitly passed to it. Android does not allow an ordinary third-party app to intercept and rewrite every other app's playback stream.

A production system-wide implementation generally needs one of:
1. OEM/vendor integration as an Android Audio Effect in the audio HAL / effect framework, with platform signing and system image changes.
2. A compatible rooted/custom-ROM integration, with device-specific audio policy and effect configuration.
3. A per-session integration for apps/audio sessions that expose a compatible audio session. This is not universal and is not system-wide.

Android playback capture (`AudioPlaybackCapture`) is not a universal output-injection mechanism. It is permission- and policy-limited and cannot transparently replace all system playback.

## Build
1. Open this folder in Android Studio.
2. Install Android SDK Platform 35, NDK, and CMake 3.22.1 from SDK Manager.
3. Use JDK 17.
4. Sync Gradle, then Build > Build Bundle(s) / APK(s) > Build APK(s).

No Gradle wrapper is included. Android Studio can import the project and use its configured Gradle installation, or generate a wrapper from a local Gradle 8.7 installation.

## Next engineering milestones
- Add PCM loopback test harness with WAV import/export and objective measurements.
- Add multiband compressor, limiter with look-ahead, and preset serialization.
- Add audio-session backend for compatible sessions.
- Investigate the exact iQOO/OriginOS 6 firmware audio-effect framework before attempting privileged integration. Do not flash generic audio-effect libraries or overwrite vendor audio configuration blindly.
