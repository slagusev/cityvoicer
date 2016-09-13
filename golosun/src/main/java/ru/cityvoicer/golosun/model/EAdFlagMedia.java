package ru.cityvoicer.golosun.model;

public enum EAdFlagMedia {
    NEWS(1),
    BANNER(2),
    VIDEO(3),
    AB(4);

    private int mValue;

    EAdFlagMedia(int i) {
        mValue = i;
    }

    public int get() {
        return mValue;
    }
}