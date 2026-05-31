package com.reminder.overlay;

public class AppConstants {

    // SharedPreferences
    public static final String PREFS_NAME = "VideoReminderPrefs";
    public static final String KEY_VIDEO_PATH = "video_path";
    public static final String KEY_TITLE = "reminder_title";
    public static final String KEY_MESSAGE = "reminder_message";
    public static final String KEY_APP_TIKTOK = "app_tiktok";
    public static final String KEY_APP_INSTAGRAM = "app_instagram";
    public static final String KEY_APP_YOUTUBE = "app_youtube";
    public static final String KEY_APP_TWITTER = "app_twitter";
    public static final String KEY_MASTER_ENABLE = "master_enable";
    public static final String KEY_DISMISSED_TODAY = "dismissed_today";
    public static final String KEY_SNOOZE_UNTIL = "snooze_until";

    // Broadcast Actions
    public static final String ACTION_SHOW_OVERLAY = "com.reminder.overlay.SHOW_OVERLAY";
    public static final String ACTION_HIDE_OVERLAY = "com.reminder.overlay.HIDE_OVERLAY";

    // Extras
    public static final String EXTRA_APP_NAME = "app_name";

    // Notification Channel
    public static final String CHANNEL_ID = "video_reminder_channel";
    public static final int NOTIFICATION_ID = 1001;

    // Package names aplikasi target
    public static final String PKG_TIKTOK = "com.zhiliaoapp.musically";
    public static final String PKG_TIKTOK_ALT = "com.ss.android.ugc.trill";
    public static final String PKG_INSTAGRAM = "com.instagram.android";
    public static final String PKG_YOUTUBE = "com.google.android.youtube";
    public static final String PKG_TWITTER = "com.twitter.android";
    public static final String PKG_TWITTER_X = "com.x.android";
}
