package ru.cityvoicer.golosun.model;

import android.content.Context;

import ru.cityvoicer.golosun.GolosunApp;

public class Settings {
    private static String PREF_FILE = "settings";
    private static String PREF_FILE_PREF_SOUND_ENABLED = "sound_enabled";
    private static String PREF_FILE_PREF_VIDEO_ENABLED = "video_enabled";

    public static boolean isSoundEnabled() {
        return GolosunApp.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).getBoolean(PREF_FILE_PREF_SOUND_ENABLED, true);
    }

    public static void setSoundEnabled(boolean b) {
        if (isSoundEnabled() == b)
            return;
        GolosunApp.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit().putBoolean(PREF_FILE_PREF_SOUND_ENABLED, b).commit();
    }

    public static boolean isVideoEnabled() {
        return GolosunApp.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).getBoolean(PREF_FILE_PREF_VIDEO_ENABLED, true);
    }

    public static void setVideoEnabled(boolean b) {
        if (isVideoEnabled() == b)
            return;
        GolosunApp.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit().putBoolean(PREF_FILE_PREF_VIDEO_ENABLED, b).commit();
    }
}
