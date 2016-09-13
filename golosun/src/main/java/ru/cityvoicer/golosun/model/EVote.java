package ru.cityvoicer.golosun.model;

public enum EVote {
    BAD(2),
    GOOD(1);

    private int mValue;

    EVote(int i) {
        mValue = i;
    }

    public int get() {
        return mValue;
    }
}
