package com.example.learnquest.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.content.SharedPreferences;

/**
 * SoundManager — lightweight singleton wrapper around SoundPool.
 *
 * Usage (any Activity):
 *   SoundManager.get(this).playCorrect();
 *   SoundManager.get(this).playWrong();
 *   SoundManager.get(this).playClick();
 *   SoundManager.get(this).playWin();
 *   SoundManager.get(this).playLose();
 *   SoundManager.get(this).playStreak();
 *   SoundManager.get(this).playStart();
 *
 * Sounds are generated programmatically via AudioTrack so no raw assets
 * are needed — just drop this file in and it works immediately.
 *
 * To mute/unmute (e.g. from a settings toggle):
 *   SoundManager.get(this).setSoundEnabled(false);
 */
public class SoundManager {

    private static SoundManager instance;

    private final SoundPool soundPool;
    private final int[] soundIds = new int[SoundType.values().length];
    private boolean soundEnabled = true;
    private final SharedPreferences prefs;

    public enum SoundType {
        CLICK, CORRECT, WRONG, WIN, LOSE, STREAK, START
    }

    // ── Singleton ─────────────────────────────────────────────────────────────
    public static synchronized SoundManager get(Context ctx) {
        if (instance == null) {
            instance = new SoundManager(ctx.getApplicationContext());
        }
        return instance;
    }

    private SoundManager(Context ctx) {
        prefs = ctx.getSharedPreferences("learnquest_prefs", Context.MODE_PRIVATE);
        soundEnabled = prefs.getBoolean("sound_enabled", true);

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build();

        loadSounds(ctx);
    }

    private void loadSounds(Context ctx) {
        // Generate PCM tones into temp files and load them.
        // Each tone is a short sine/square wave burst written to cache.
        for (SoundType type : SoundType.values()) {
            java.io.File f = generateTone(ctx, type);
            if (f != null) {
                soundIds[type.ordinal()] = soundPool.load(f.getAbsolutePath(), 1);
            }
        }
    }

    // ── Public playback methods ───────────────────────────────────────────────
    public void playClick()   { play(SoundType.CLICK); }
    public void playCorrect() { play(SoundType.CORRECT); }
    public void playWrong()   { play(SoundType.WRONG); }
    public void playWin()     { play(SoundType.WIN); }
    public void playLose()    { play(SoundType.LOSE); }
    public void playStreak()  { play(SoundType.STREAK); }
    public void playStart()   { play(SoundType.START); }

    public void play(SoundType type) {
        if (!soundEnabled) return;
        int id = soundIds[type.ordinal()];
        if (id != 0) {
            soundPool.play(id, 1f, 1f, 1, 0, 1f);
        }
    }

    public boolean isSoundEnabled() { return soundEnabled; }

    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        prefs.edit().putBoolean("sound_enabled", enabled).apply();
    }

    public void toggleSound() {
        setSoundEnabled(!soundEnabled);
    }

    public void release() {
        soundPool.release();
        instance = null;
    }

    // ── Tone generation ───────────────────────────────────────────────────────
    // Generates a simple WAV file for each sound type using pure math.
    // No external audio assets required.

    private java.io.File generateTone(Context ctx, SoundType type) {
        try {
            java.io.File dir = ctx.getCacheDir();
            java.io.File f   = new java.io.File(dir, "snd_" + type.name().toLowerCase() + ".wav");
            if (f.exists()) return f;  // already generated this session

            float[] params = getToneParams(type); // freq1, freq2, durationMs, volume
            float freq1  = params[0];
            float freq2  = params[1];
            int   ms     = (int) params[2];
            float vol    = params[3];
            boolean sweep = params[4] > 0;

            int sampleRate = 22050;
            int numSamples = (sampleRate * ms) / 1000;
            byte[] pcm = new byte[numSamples * 2]; // 16-bit mono

            for (int i = 0; i < numSamples; i++) {
                double t    = (double) i / sampleRate;
                double freq = sweep ? (freq1 + (freq2 - freq1) * ((double) i / numSamples)) : freq1;
                // Second tone (harmony) for richer sound
                double freq3 = type == SoundType.WIN ? freq * 1.25 : (type == SoundType.STREAK ? freq * 1.5 : freq);
                double sample = Math.sin(2 * Math.PI * freq * t);
                if (type == SoundType.WIN || type == SoundType.STREAK) {
                    sample = (sample + 0.5 * Math.sin(2 * Math.PI * freq3 * t)) / 1.5;
                }
                // Envelope: quick attack, gentle decay
                double env = Math.min(1.0, 5.0 * t) * Math.exp(-3.0 * t / (ms / 1000.0));
                if (type == SoundType.CLICK) env = Math.exp(-20.0 * t);

                short s = (short) (sample * env * vol * Short.MAX_VALUE);
                pcm[i * 2]     = (byte) (s & 0xFF);
                pcm[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
            }

            writeWav(f, pcm, sampleRate);
            return f;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns tone parameters per sound type:
     * [freq1, freq2, durationMs, volume, sweep(1=yes)]
     */
    private float[] getToneParams(SoundType type) {
        switch (type) {
            case CLICK:   return new float[]{ 800, 800,  60, 0.5f, 0 };
            case CORRECT: return new float[]{ 523, 659, 300, 0.8f, 0 };  // C -> E
            case WRONG:   return new float[]{ 300, 200, 350, 0.7f, 1 };  // descending
            case WIN:     return new float[]{ 440, 880, 500, 0.9f, 1 };  // ascending sweep
            case LOSE:    return new float[]{ 400, 250, 500, 0.7f, 1 };  // sad descend
            case STREAK:  return new float[]{ 660, 880, 250, 0.85f,1 };  // bright up
            case START:   return new float[]{ 350, 700, 400, 0.8f, 1 };  // power-up
            default:      return new float[]{ 440, 440, 200, 0.5f, 0 };
        }
    }

    private void writeWav(java.io.File f, byte[] pcm, int sampleRate) throws Exception {
        int dataLen = pcm.length;
        java.io.FileOutputStream out = new java.io.FileOutputStream(f);
        // RIFF header
        out.write("RIFF".getBytes());
        writeInt(out, 36 + dataLen);
        out.write("WAVE".getBytes());
        // fmt chunk
        out.write("fmt ".getBytes());
        writeInt(out, 16);
        writeShort(out, (short) 1);           // PCM
        writeShort(out, (short) 1);           // mono
        writeInt(out, sampleRate);
        writeInt(out, sampleRate * 2);        // byte rate
        writeShort(out, (short) 2);           // block align
        writeShort(out, (short) 16);          // bits per sample
        // data chunk
        out.write("data".getBytes());
        writeInt(out, dataLen);
        out.write(pcm);
        out.close();
    }

    private void writeInt(java.io.OutputStream out, int v) throws Exception {
        out.write(v & 0xFF); out.write((v >> 8) & 0xFF);
        out.write((v >> 16) & 0xFF); out.write((v >> 24) & 0xFF);
    }
    private void writeShort(java.io.OutputStream out, short v) throws Exception {
        out.write(v & 0xFF); out.write((v >> 8) & 0xFF);
    }
}
