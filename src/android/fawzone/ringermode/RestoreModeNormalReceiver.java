package org.fawzone.ringermode;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import de.appplant.cordova.plugin.localnotification.TriggerReceiver;
import de.appplant.cordova.plugin.notification.Manager;
import de.appplant.cordova.plugin.notification.Notification;

import static android.content.Context.POWER_SERVICE;

public class RestoreModeNormalReceiver extends BroadcastReceiver {
    public static final String TAG = "RestoreModeNormal";

    public static final String MSG_NORMAL_RESTORED = "MSG_NORMAL_RESTORED";
    public static final String ICON_NORMAL_RESTORED = "ICON_NORMAL_RESTORED";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.i(TAG, "onReceive triggered with intent: " + intent);

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager == null) {
                Log.e(TAG, "AudioManager is null");
                return;
            }

            // Si le téléphone est déjà en mode NORMAL, rien à faire
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                Log.i(TAG, "Déjà en RINGER_MODE_NORMAL ==> do nothing");
                return;
            }

            wakeUp(context);

            // Vérification de la permission Ne Pas Déranger (DND / Access Notification Policy)
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            boolean hasDndAccess = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager != null) {
                hasDndAccess = notificationManager.isNotificationPolicyAccessGranted();
            }

            if (hasDndAccess) {
                try {
                    Intent serviceIntent = new Intent(context, VibrateForegroundService.class);
                    serviceIntent.setAction(VibrateForegroundService.ACTION_SET_NORMAL);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent);
                    } else {
                        context.startService(serviceIntent);
                    }
                    Log.i(TAG, "VibrateForegroundService démarré pour RINGER_MODE_NORMAL !");
                } catch (Exception se) {
                    Log.e(TAG, "Erreur lors du démarrage du service de changement de ringerMode", se);
                    try {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    } catch (Exception e) {
                        Log.e(TAG, "Fallback setRingerMode error", e);
                    }
                }
            } else {
                Log.w(TAG, "Permission Ne Pas Déranger (DND) non accordée. Impossible de changer le ringerMode.");
            }

            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }

            final String msgRingerModeNormalRestored = bundle.getString(MSG_NORMAL_RESTORED);
            final String iconRingerModeNormal = bundle.getString(ICON_NORMAL_RESTORED);
            int toastId = bundle.getInt(Notification.EXTRA_ID, 0);

            if (msgRingerModeNormalRestored != null && !msgRingerModeNormalRestored.isEmpty()) {
                Log.i(TAG, msgRingerModeNormalRestored);
                Toast.makeText(context.getApplicationContext(), msgRingerModeNormalRestored, Toast.LENGTH_LONG).show();
            }

            if (toastId != 0 && iconRingerModeNormal != null) {
                Manager manager = Manager.getInstance(context);
                JSONObject update = new JSONObject();
                update.put("icon", iconRingerModeNormal);
                update.put("text", msgRingerModeNormalRestored);

                manager.update(toastId, update, TriggerReceiver.class);
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception dans RestoreModeNormalReceiver: ", e);
        }
    }

    /**
     * Wakeup the device.
     *
     * @param context The application context.
     */
    private void wakeUp(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            if (pm == null) return;

            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, "awqat:myWakeLock");
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire(1000);
            wakeLock.release();
        } catch (Exception e) {
            Log.e(TAG, "Error in wakeUp", e);
        }
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, RestoreModeNormalReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(sender);
        }
    }
}
