package ru.cityvoicer.golosun.model;

public enum ESex {
    MALE("1"),
    FEMALE("2");

    private String mValue;

    ESex(String i) {
        mValue = i;
    }

    public String get() {
        return mValue;
    }
}