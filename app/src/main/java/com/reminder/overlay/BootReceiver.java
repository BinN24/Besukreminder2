package com.reminder.overlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        SharedPreferences prefs = context.getSharedPreferences(
                AppConstants.PREFS_NAME, Context.MODE_PRIVATE);

        // Hanya start service jika master switch aktif
        if (prefs.getBoolean(AppConstants.KEY_MASTER_ENABLE, false)) {
            Intent serviceIntent = new Intent(context, OverlayService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
