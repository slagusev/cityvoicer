package ru.cityvoicer.golosun;

public class Constants {
    static public final String ApiUrl = "https://golosun.ru";
    static public final String ImageUrl = "http://golosun.ru";
    static public final int TargetAdsCount = 12;
    static public final String HockeyAppAppId = "7c45a4495ce74ee784d451f64cafb351";
    static public final String GcmSenderId = "1026162771278";

    public static final boolean isDebugBuild() {
        return BuildConfig.VERSION_NAME.endsWith("d");
    }

    public static final boolean isTestBuild() {
        return BuildConfig.VERSION_NAME.endsWith("t");
    }

    public static final boolean isReleaseBuild() {
        return !isTestBuild() && !isDebugBuild();
    }

    public static String fixImageUrl(String url) {
        if (url == null || url.isEmpty())
            return null;
        if (url.startsWith("http")) {
            return url;
        } else if (url.startsWith("/")) {
            return ImageUrl + url;
        } else {
            return ImageUrl + "/" + url;
        }
    }
}
