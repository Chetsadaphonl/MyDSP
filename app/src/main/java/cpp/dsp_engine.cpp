#include <jni.h>
#include <array>
#include <cmath>
#include <algorithm>

namespace {
constexpr int kBands = 5;
constexpr float kQ = 1.0f;
constexpr float kPi = 3.14159265358979323846f;
constexpr float kFreq[kBands] = {60.0f, 250.0f, 1000.0f, 4000.0f, 12000.0f};

struct Biquad {
    float b0=1, b1=0, b2=0, a1=0, a2=0;
    float z1=0, z2=0;

    void configure(float sampleRate, float freq, float gainDb) {
        const float A = std::pow(10.0f, gainDb / 40.0f);
        const float w0 = 2.0f * kPi * std::min(freq, sampleRate * 0.45f) / sampleRate;
        const float cw = std::cos(w0), sw = std::sin(w0);
        const float alpha = sw / (2.0f * kQ);
        const float a0 = 1.0f + alpha / A;
        b0 = (1.0f + alpha * A) / a0;
        b1 = (-2.0f * cw) / a0;
        b2 = (1.0f - alpha * A) / a0;
        a1 = (-2.0f * cw) / a0;
        a2 = (1.0f - alpha / A) / a0;
    }

    float run(float x) {
        float y = b0*x + z1;
        z1 = b1*x - a1*y + z2;
        z2 = b2*x - a2*y;
        return y;
    }
};

std::array<float, kBands> gains{};
float preampDb = 0.0f;
std::array<std::array<Biquad, kBands>, 8> filters{};
int configuredRate = 0;
int configuredChannels = 0;

void configure(int rate, int channels) {
    if (rate == configuredRate && channels == configuredChannels) return;
    configuredRate = rate;
    configuredChannels = channels;
    for (int c=0; c<std::min(channels, 8); ++c)
        for (int b=0; b<kBands; ++b)
            filters[c][b].configure((float)rate, kFreq[b], gains[b]);
}
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_mydsp_NativeDsp_setBand(JNIEnv*, jclass, jint index, jfloat gainDb) {
    if (index < 0 || index >= kBands) return;
    gains[index] = std::clamp(gainDb, -12.0f, 12.0f);
    configuredRate = 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_mydsp_NativeDsp_setPreamp(JNIEnv*, jclass, jfloat gainDb) {
    preampDb = std::clamp(gainDb, -20.0f, 12.0f);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_mydsp_NativeDsp_reset(JNIEnv*, jclass) {
    gains.fill(0.0f);
    preampDb = 0.0f;
    configuredRate = 0;
    for (auto &channel : filters)
        for (auto &filter : channel) { filter.z1 = 0; filter.z2 = 0; }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_mydsp_NativeDsp_process(JNIEnv* env, jclass, jfloatArray pcm,
                                          jint channels, jint sampleRate) {
    if (!pcm || channels < 1 || channels > 8 || sampleRate < 8000) return;
    jsize count = env->GetArrayLength(pcm);
    if (count % channels != 0) return;
    jfloat* data = env->GetFloatArrayElements(pcm, nullptr);
    if (!data) return;

    configure(sampleRate, channels);
    const float preamp = std::pow(10.0f, preampDb / 20.0f);
    for (jsize i=0; i<count; ++i) {
        int channel = i % channels;
        float x = data[i] * preamp;
        for (int b=0; b<kBands; ++b) x = filters[channel][b].run(x);
        data[i] = std::clamp(x, -1.0f, 1.0f);
    }
    env->ReleaseFloatArrayElements(pcm, data, 0);
}
