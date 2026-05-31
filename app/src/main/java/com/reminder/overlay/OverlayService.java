package com.reminder.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;
import android.media.MediaPlayer;
import android.content.SharedPreferences;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;

import androidx.core.app.NotificationCompat;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private VideoView overlayVideoView;
    private boolean isOverlayShowing = false;

    private BroadcastReceiver showOverlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String appName = intent.getStringExtra(AppConstants.EXTRA_APP_NAME);
            showOverlay(appName);
        }
    };

    private BroadcastReceiver hideOverlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideOverlay();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Register broadcast receivers
        IntentFilter showFilter = new IntentFilter(AppConstants.ACTION_SHOW_OVERLAY);
        registerReceiver(showOverlayReceiver, showFilter);

        IntentFilter hideFilter = new IntentFilter(AppConstants.ACTION_HIDE_OVERLAY);
        registerReceiver(hideOverlayReceiver, hideFilter);

        startForegroundWithNotification();
    }

    private void startForegroundWithNotification() {
        // Buat notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    AppConstants.CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getString(R.string.channel_desc));
            channel.setShowBadge(false);
            channel.setSound(null, null);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        // Intent untuk buka app saat klik notifikasi
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
                .setContentTitle("Video Reminder Aktif")
                .setContentText("Mendeteksi aplikasi TikTok/Instagram...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(AppConstants.NOTIFICATION_ID, notification);
    }

    private void showOverlay(String appName) {
        if (isOverlayShowing) return;

        SharedPreferences prefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        String videoPath = prefs.getString(AppConstants.KEY_VIDEO_PATH, "");
        String title = prefs.getString(AppConstants.KEY_TITLE, "STOP! Kamu harus ingat sesuatu!");
        String message = prefs.getString(AppConstants.KEY_MESSAGE, "Jangan lupa tugasmu!");

        if (videoPath.isEmpty()) return;

        // Inflate overlay layout
        LayoutInflater inflater = LayoutInflater.from(this);
        overlayView = inflater.inflate(R.layout.overlay_video, null);

        // Setup WindowManager params - muncul di atas SEMUA aplikasi
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        // Set text
        TextView tvTitle = overlayView.findViewById(R.id.tvOverlayTitle);
        TextView tvMessage = overlayView.findViewById(R.id.tvOverlayMessage);
        tvTitle.setText(title);
        tvMessage.setText(appName != null ?
                "Kamu baru saja buka " + appName + ". " + message : message);

        // Setup video
        overlayVideoView = overlayView.findViewById(R.id.overlayVideoView);
        Uri videoUri = Uri.parse(videoPath);
        overlayVideoView.setVideoURI(videoUri);
        overlayVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            overlayVideoView.start();
        });
        overlayVideoView.setOnErrorListener((mp, what, extra) -> {
            // Video error - tetap tampilkan overlay tanpa video
            return true;
        });

        // Tombol tutup (X)
        TextView tvClose = overlayView.findViewById(R.id.tvClose);
        tvClose.setOnClickListener(v -> hideOverlay());

        // Tombol "Pergi Sekarang" - tutup overlay dan biarkan user keluar
        Button btnGoNow = overlayView.findViewById(R.id.btnGoNow);
        btnGoNow.setOnClickListener(v -> {
            hideOverlay();
            // Buka home screen
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        });

        // Tombol Snooze - tunda 15 menit
        Button btnSnooze = overlayView.findViewById(R.id.btnSnooze);
        btnSnooze.setOnClickListener(v -> {
            long snoozeUntil = System.currentTimeMillis() + (15 * 60 * 1000); // 15 menit
            prefs.edit().putLong(AppConstants.KEY_SNOOZE_UNTIL, snoozeUntil).apply();
            hideOverlay();
        });

        // Tombol Abaikan Hari Ini
        Button btnDismiss = overlayView.findViewById(R.id.btnDismiss);
        btnDismiss.setOnClickListener(v -> {
            // Simpan dismissed hari ini
            String today = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + "-" +
                    java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + "-" +
                    java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH);
            prefs.edit().putString(AppConstants.KEY_DISMISSED_TODAY, today).apply();
            hideOverlay();
        });

        // Setup drag to dismiss (swipe down)
        setupDragToDismiss(overlayView);

        // Tambahkan ke window
        windowManager.addView(overlayView, params);
        isOverlayShowing = true;

        // Animasi masuk - slide dari bawah
        View container = overlayView.findViewById(R.id.overlayContainer);
        container.setTranslationY(1000f);
        container.animate()
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        // Animasi kedip pada icon peringatan
        startWarningBlink(overlayView.findViewById(R.id.tvWarningIcon));
    }

    private void setupDragToDismiss(View overlay) {
        View container = overlay.findViewById(R.id.overlayContainer);
        final float[] startY = {0};
        final float[] startTranslation = {0};

        // Background hitam - klik untuk tutup
        overlay.setOnClickListener(v -> {
            // Klik luar area container = tutup
            hideOverlay();
        });

        // Container - intercept klik agar tidak tutup
        container.setOnClickListener(v -> {
            // Do nothing - jangan tutup
        });
    }

    private void startWarningBlink(View view) {
        if (view == null) return;
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.2f, 1f);
        alpha.setDuration(1000);
        alpha.setRepeatCount(ObjectAnimator.INFINITE);
        alpha.start();
    }

    private void hideOverlay() {
        if (!isOverlayShowing || overlayView == null) return;

        // Animasi keluar - slide ke bawah
        View container = overlayView.findViewById(R.id.overlayContainer);
        if (container != null) {
            container.animate()
                    .translationY(1200f)
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        // Stop video
                        if (overlayVideoView != null) {
                            overlayVideoView.stopPlayback();
                        }
                        // Remove dari window
                        try {
                            windowManager.removeView(overlayView);
                        } catch (Exception ignored) {}
                        overlayView = null;
                        isOverlayShowing = false;
                    })
                    .start();
        } else {
            try {
                if (overlayVideoView != null) overlayVideoView.stopPlayback();
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {}
            overlayView = null;
            isOverlayShowing = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideOverlay();
        try {
            unregisterReceiver(showOverlayReceiver);
            unregisterReceiver(hideOverlayReceiver);
        } catch (Exception ignored) {}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
