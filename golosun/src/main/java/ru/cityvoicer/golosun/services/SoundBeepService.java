package ru.cityvoicer.golosun.services;


import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

import ru.cityvoicer.golosun.GolosunApp;
import ru.cityvoicer.golosun.model.Settings;

public class SoundBeepService {
    private SoundPool mSoundPool;
    private static class SoundInfo {
        public int soundId;
        public boolean loaded;
    }
    private HashMap<Integer, SoundInfo> mSounds = new HashMap<Integer, SoundInfo>();
    private static SoundBeepService mInstance;

    public float volume = 1.0f;

    public static SoundBeepService getInstance() {
        if (mInstance == null) {
            mInstance = new SoundBeepService();
        }
        return mInstance;
    }

    private SoundBeepService() {
        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sound, int status) {
                if (status == 0) {
                    synchronized (SoundBeepService.this) {
                        for (Map.Entry<Integer, SoundInfo> kvp : mSounds.entrySet()) {
                            if (kvp.getValue().soundId == sound) {
                                kvp.getValue().loaded = true;
                                beep(kvp.getKey());
                            }
                        }
                    }
                }
            }
        });
    }

    public synchronized void beep(final int resId) {
        if (!Settings.isSoundEnabled())
            return;
        if (mSounds.containsKey(resId)) {
            SoundInfo info = mSounds.get(resId);
            if (info.loaded) {
                mSoundPool.play(info.soundId, 1.0f, 1.0f, 0, 0, volume);
            }
        } else {
            SoundInfo info = new SoundInfo();
            info.soundId = mSoundPool.load(GolosunApp.getContext(), resId, 0);
            mSounds.put(resId, info);
        }
    }
}
