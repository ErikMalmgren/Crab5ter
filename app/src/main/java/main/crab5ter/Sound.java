package main.crab5ter;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class Sound {
    private SoundPool soundPool;
    private int deathSound;
    private int winSound;
    private int crashSound;
    private Context context;

    public Sound(Context context) {
        this.context = context;
        loadSounds();
    }

    private void loadSounds() {
        SoundPool.Builder builder = new SoundPool.Builder();
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        builder.setAudioAttributes(attributes).setMaxStreams(10);
        soundPool = builder.build();
        deathSound = soundPool.load(context, R.raw.death_sound, 1);
        winSound = soundPool.load(context, R.raw.win_sound, 1);
        crashSound = soundPool.load(context, R.raw.crash_sound, 1);
    }
    public void playDeathSound() {
        soundPool.play(deathSound, 1, 1, 0, 0, 1.0f);
    }
    public void playWinSound() {
        soundPool.play(winSound, 1, 1, 0, 0, 1.0f);
    }
    public void playCrashSound() {
        soundPool.play(crashSound, 0.5f, 0.5f, 0, 0, 1.0f);
    }

}
