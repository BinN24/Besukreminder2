package com.reminder.overlay;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Accessibility Service yang mendeteksi ketika pengguna membuka aplikasi tertentu
 * (TikTok, Instagram, dll) dan memicu overlay video pengingat.
 *
 * Cara kerja:
 * - Mendengarkan event TYPE_WINDOW_STATE_CHANGED
 * - Mengecek package name dari window yang baru aktif
 * - Jika package cocok dengan target dan reminder aktif, tampilkan overlay
 */
public class AppDetectorAccessibilityService extends AccessibilityService {

    private String lastDetectedPackage = "";
    private static final long OVERLAY_COOLDOWN_MS = 30 * 1000; // 30 detik cooldown
    private long lastOverlayShowTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        // Hanya proses event pergantian window
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        CharSequence packageName = event.getPackageName();
        if (packageName == null) return;

        String pkg = packageName.toString();

        // Hindari trigger berulang dari package yang sama
        if (pkg.equals(lastDetectedPackage)) return;

        // Jangan trigger untuk app kita sendiri
        if (pkg.equals(getPackageName())) return;

        // Cek apakah package ini adalah target
        if (isTargetApp(pkg)) {
            lastDetectedPackage = pkg;
            handleTargetAppOpened(pkg);
        } else {
            // Reset ketika user pindah ke app lain
            lastDetectedPackage = "";
        }
    }

    private boolean isTargetApp(String packageName) {
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);

        // Cek apakah master switch aktif
        if (!prefs.getBoolean(AppConstants.KEY_MASTER_ENABLE, false)) return false;

        // Cek apakah video sudah dipilih
        String videoPath = prefs.getString(AppConstants.KEY_VIDEO_PATH, "");
        if (videoPath.isEmpty()) return false;

        // Cek masing-masing target app
        if (prefs.getBoolean(AppConstants.KEY_APP_TIKTOK, true)) {
            if (packageName.equals(AppConstants.PKG_TIKTOK) ||
                    packageName.equals(AppConstants.PKG_TIKTOK_ALT) ||
                    packageName.contains("tiktok") || packageName.contains("musically")) {
                return true;
            }
        }

        if (prefs.getBoolean(AppConstants.KEY_APP_INSTAGRAM, true)) {
            if (packageName.equals(AppConstants.PKG_INSTAGRAM) ||
                    packageName.contains("instagram")) {
                return true;
            }
        }

        if (prefs.getBoolean(AppConstants.KEY_APP_YOUTUBE, false)) {
            if (packageName.equals(AppConstants.PKG_YOUTUBE) ||
                    packageName.contains("youtube")) {
                return true;
            }
        }

        if (prefs.getBoolean(AppConstants.KEY_APP_TWITTER, false)) {
            if (packageName.equals(AppConstants.PKG_TWITTER) ||
                    packageName.equals(AppConstants.PKG_TWITTER_X) ||
                    packageName.contains("twitter")) {
                return true;
            }
        }

        return false;
    }

    private void handleTargetAppOpened(String packageName) {
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);

        // Cek apakah sudah di-dismiss hari ini
        String dismissedToday = prefs.getString(AppConstants.KEY_DISMISSED_TODAY, "");
        String today = getTodayString();
        if (dismissedToday.equals(today)) return;

        // Cek snooze
        long snoozeUntil = prefs.getLong(AppConstants.KEY_SNOOZE_UNTIL, 0);
        if (System.currentTimeMillis() < snoozeUntil) return;

        // Cek cooldown (jangan tampilkan terlalu sering)
        long now = System.currentTimeMillis();
        if (now - lastOverlayShowTime < OVERLAY_COOLDOWN_MS) return;
        lastOverlayShowTime = now;

        // Tampilkan overlay
        String appName = getAppFriendlyName(packageName);
        Intent intent = new Intent(AppConstants.ACTION_SHOW_OVERLAY);
        intent.putExtra(AppConstants.EXTRA_APP_NAME, appName);
        sendBroadcast(intent);
    }

    private String getAppFriendlyName(String packageName) {
        if (packageName.contains("tiktok") || packageName.contains("musically")) return "TikTok";
        if (packageName.contains("instagram")) return "Instagram";
        if (packageName.contains("youtube")) return "YouTube";
        if (packageName.contains("twitter")) return "X (Twitter)";
        return packageName;
    }

    private String getTodayString() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "-" +
                cal.get(Calendar.MONTH) + "-" +
                cal.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public void onInterrupt() {
        // Service terganggu
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Service berhasil terhubung
    }
}
