package org.fawzone.ringermode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class VibrateForegroundService extends Service {
    public static final String TAG = "VibrateFGService";
    private static final String CHANNEL_ID = "bg_ringer_channel";

    public static final String ACTION_SET_VIBRATE = "org.fawzone.ringermode.ACTION_SET_VIBRATE";
    public static final String ACTION_SET_NORMAL = "org.fawzone.ringermode.ACTION_SET_NORMAL";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Awqat")
                .setContentText("Gestion du mode sonnerie...")
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1002, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(1002, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting foreground service", e);
        }

        try {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            boolean hasDndAccess = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (nm != null && nm.isNotificationPolicyAccessGranted());

            if (hasDndAccess && audioManager != null) {
                String action = intent != null ? intent.getAction() : null;
                if (ACTION_SET_NORMAL.equals(action)) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    Log.i(TAG, "RINGER_MODE_NORMAL appliqué avec succès en arrière-plan !");
                } else {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    Log.i(TAG, "RINGER_MODE_VIBRATE appliqué avec succès en arrière-plan !");
                }
            } else {
                Log.w(TAG, "Permission DND non accordée par l'utilisateur.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du changement de mode sonnerie", e);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE);
            } else {
                stopForeground(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping foreground", e);
        }
        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Service Mode Sonnerie",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }
}
