package com.example.mydsp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final String[] bandNames = {"60 Hz · SUB BASS", "250 Hz · BASS", "1 kHz · MID", "4 kHz · PRESENCE", "12 kHz · TREBLE"};
    private final int[] gains = new int[5];
    private TextView status;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setBackgroundColor(Color.rgb(12, 12, 18));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(20));

        TextView title = label("MYDSP ENGINE", 25, Color.WHITE, true);
        page.addView(title);
        TextView subtitle = label("JAVA UI  /  C++ NATIVE DSP CORE", 11, Color.rgb(174, 130, 255), true);
        subtitle.setPadding(0, dp(5), 0, dp(22));
        page.addView(subtitle);

        TextView info = label("5-BAND PEAKING EQ", 14, Color.WHITE, true);
        page.addView(info);

        for (int i = 0; i < bandNames.length; i++) {
            final int index = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(12), 0, dp(5));

            TextView name = label(bandNames[i] + "     0.0 dB", 12, Color.LTGRAY, true);
            row.addView(name);

            SeekBar seek = new SeekBar(this);
            seek.setMax(240);
            seek.setProgress(120);
            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                    float db = (value - 120) / 10.0f;
                    gains[index] = value - 120;
                    NativeDsp.setBand(index, db);
                    name.setText(bandNames[index] + String.format(java.util.Locale.US, "     %.1f dB", db));
                }
                @Override public void onStartTrackingTouch(SeekBar bar) {}
                @Override public void onStopTrackingTouch(SeekBar bar) {}
            });
            row.addView(seek, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));
            page.addView(row);
        }

        status = label("ENGINE READY · PCM PROCESSOR CORE", 11, Color.rgb(100, 220, 170), true);
        status.setPadding(0, dp(24), 0, dp(8));
        page.addView(status);

        TextView note = label("This screen configures the native DSP engine. System-wide audio routing requires a privileged Android audio-effect integration or a compatible rooted system build.", 12, Color.GRAY, false);
        page.addView(note);

        setContentView(page);
    }

    private TextView label(String text, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
