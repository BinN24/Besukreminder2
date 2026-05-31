package com.reminder.overlay;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.media.MediaController;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 1001;
    private static final int REQUEST_PICK_VIDEO = 1002;

    private TextView tvOverlayStatus, tvAccessibilityStatus, tvVideoPath, tvMasterStatus;
    private Button btnOverlayPermission, btnAccessibilityPermission, btnPickVideo, btnSave, btnTest;
    private Switch switchTikTok, switchInstagram, switchYouTube, switchTwitter, switchMasterEnable;
    private EditText etReminderTitle, etReminderMessage;
    private VideoView videoPreview;
    private android.widget.FrameLayout llNoVideo;

    private SharedPreferences prefs;
    private String selectedVideoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);

        initViews();
        loadSavedSettings();
        setupClickListeners();
        updatePermissionStatuses();
    }

    private void initViews() {
        tvOverlayStatus = findViewById(R.id.tvOverlayStatus);
        tvAccessibilityStatus = findViewById(R.id.tvAccessibilityStatus);
        tvVideoPath = findViewById(R.id.tvVideoPath);
        tvMasterStatus = findViewById(R.id.tvMasterStatus);

        btnOverlayPermission = findViewById(R.id.btnOverlayPermission);
        btnAccessibilityPermission = findViewById(R.id.btnAccessibilityPermission);
        btnPickVideo = findViewById(R.id.btnPickVideo);
        btnSave = findViewById(R.id.btnSave);
        btnTest = findViewById(R.id.btnTest);

        switchTikTok = findViewById(R.id.switchTikTok);
        switchInstagram = findViewById(R.id.switchInstagram);
        switchYouTube = findViewById(R.id.switchYouTube);
        switchTwitter = findViewById(R.id.switchTwitter);
        switchMasterEnable = findViewById(R.id.switchMasterEnable);

        etReminderTitle = findViewById(R.id.etReminderTitle);
        etReminderMessage = findViewById(R.id.etReminderMessage);
        videoPreview = findViewById(R.id.videoPreview);
        llNoVideo = findViewById(R.id.llNoVideo);
    }

    private void loadSavedSettings() {
        selectedVideoPath = prefs.getString(AppConstants.KEY_VIDEO_PATH, "");
        etReminderTitle.setText(prefs.getString(AppConstants.KEY_TITLE,
                "STOP! Kamu harus besuk sekarang!"));
        etReminderMessage.setText(prefs.getString(AppConstants.KEY_MESSAGE,
                "TikTok bisa nanti, ini tidak bisa ditunda!"));

        switchTikTok.setChecked(prefs.getBoolean(AppConstants.KEY_APP_TIKTOK, true));
        switchInstagram.setChecked(prefs.getBoolean(AppConstants.KEY_APP_INSTAGRAM, true));
        switchYouTube.setChecked(prefs.getBoolean(AppConstants.KEY_APP_YOUTUBE, false));
        switchTwitter.setChecked(prefs.getBoolean(AppConstants.KEY_APP_TWITTER, false));
        switchMasterEnable.setChecked(prefs.getBoolean(AppConstants.KEY_MASTER_ENABLE, false));

        if (!selectedVideoPath.isEmpty()) {
            tvVideoPath.setText(selectedVideoPath);
            loadVideoPreview(Uri.parse(selectedVideoPath));
        }

        updateMasterStatus(switchMasterEnable.isChecked());
    }

    private void setupClickListeners() {
        // Izin overlay
        btnOverlayPermission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            }
        });

        // Izin accessibility
        btnAccessibilityPermission.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Cari 'Video Reminder' dan aktifkan", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        // Pilih video
        btnPickVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/mp4");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_PICK_VIDEO);
        });

        // Master switch
        switchMasterEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateMasterStatus(isChecked);
            if (isChecked) {
                if (!hasOverlayPermission() || !hasAccessibilityPermission()) {
                    Toast.makeText(this,
                            "Aktifkan semua izin terlebih dahulu!", Toast.LENGTH_LONG).show();
                    switchMasterEnable.setChecked(false);
                    return;
                }
                // Start service
                Intent serviceIntent = new Intent(this, OverlayService.class);
                startForegroundService(serviceIntent);
            } else {
                // Stop service
                Intent serviceIntent = new Intent(this, OverlayService.class);
                stopService(serviceIntent);
            }
        });

        // Simpan
        btnSave.setOnClickListener(v -> saveSettings());

        // Test overlay
        btnTest.setOnClickListener(v -> {
            if (!hasOverlayPermission()) {
                Toast.makeText(this, "Aktifkan izin overlay dulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedVideoPath.isEmpty()) {
                Toast.makeText(this, "Pilih video dulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Kirim broadcast untuk test
            Intent testIntent = new Intent(AppConstants.ACTION_SHOW_OVERLAY);
            testIntent.putExtra(AppConstants.EXTRA_APP_NAME, "Test App");
            sendBroadcast(testIntent);
            Toast.makeText(this, "Menampilkan overlay test...", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AppConstants.KEY_VIDEO_PATH, selectedVideoPath);
        editor.putString(AppConstants.KEY_TITLE, etReminderTitle.getText().toString());
        editor.putString(AppConstants.KEY_MESSAGE, etReminderMessage.getText().toString());
        editor.putBoolean(AppConstants.KEY_APP_TIKTOK, switchTikTok.isChecked());
        editor.putBoolean(AppConstants.KEY_APP_INSTAGRAM, switchInstagram.isChecked());
        editor.putBoolean(AppConstants.KEY_APP_YOUTUBE, switchYouTube.isChecked());
        editor.putBoolean(AppConstants.KEY_APP_TWITTER, switchTwitter.isChecked());
        editor.putBoolean(AppConstants.KEY_MASTER_ENABLE, switchMasterEnable.isChecked());
        editor.apply();

        Toast.makeText(this, "✅ Pengaturan tersimpan!", Toast.LENGTH_SHORT).show();
    }

    private void loadVideoPreview(Uri uri) {
        try {
            videoPreview.setVisibility(android.view.View.VISIBLE);
            if (llNoVideo != null) llNoVideo.setVisibility(android.view.View.GONE);
            videoPreview.setVideoURI(uri);
            MediaController mc = new MediaController(this);
            mc.setAnchorView(videoPreview);
            videoPreview.setMediaController(mc);
            videoPreview.start();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memuat preview video", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePermissionStatuses() {
        boolean overlayOk = hasOverlayPermission();
        boolean accessOk = hasAccessibilityPermission();

        // Overlay status
        tvOverlayStatus.setText(overlayOk ? "✓" : "✗");
        tvOverlayStatus.setTextColor(overlayOk ?
                getColor(R.color.green_ok) : getColor(R.color.red_warning));
        btnOverlayPermission.setVisibility(overlayOk ?
                android.view.View.GONE : android.view.View.VISIBLE);

        // Accessibility status
        tvAccessibilityStatus.setText(accessOk ? "✓" : "✗");
        tvAccessibilityStatus.setTextColor(accessOk ?
                getColor(R.color.green_ok) : getColor(R.color.red_warning));
        btnAccessibilityPermission.setVisibility(accessOk ?
                android.view.View.GONE : android.view.View.VISIBLE);
    }

    private void updateMasterStatus(boolean enabled) {
        if (enabled) {
            tvMasterStatus.setText("🟢 Reminder aktif - Overlay akan muncul saat buka app");
            tvMasterStatus.setTextColor(getColor(R.color.green_ok));
        } else {
            tvMasterStatus.setText("⚫ Reminder tidak aktif");
            tvMasterStatus.setTextColor(0xFF888888);
        }
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private boolean hasAccessibilityPermission() {
        AccessibilityManager am = (AccessibilityManager)
                getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().contains(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            updatePermissionStatuses();
        } else if (requestCode == REQUEST_PICK_VIDEO && resultCode == Activity.RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                // Simpan izin akses permanen
                getContentResolver().takePersistableUriPermission(videoUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                selectedVideoPath = videoUri.toString();
                tvVideoPath.setText(selectedVideoPath);
                loadVideoPreview(videoUri);
                Toast.makeText(this, "✅ Video berhasil dipilih!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatuses();
    }
}
