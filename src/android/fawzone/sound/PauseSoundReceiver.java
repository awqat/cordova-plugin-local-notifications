package org.fawzone.sound;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONObject;

import de.appplant.cordova.plugin.localnotification.LocalNotification;
import de.appplant.cordova.plugin.notification.Manager;
import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.receiver.AbstractClickReceiver;

import static android.content.Context.POWER_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;


public class PauseSoundReceiver extends AbstractClickReceiver {
    public static final String TAG = "RestoreModeNormal";

    public static final String MSG_NORMAL_RESTORED = "MSG_NORMAL_RESTORED";
    public static final String ICON_NORMAL_RESTORED = "ICON_NORMAL_RESTORED";


    @Override
    public void onClick(Notification notification, Bundle bundle) {

        try {
            Log.e(TAG, " onReceive " + notification);

            SoundManager.pauseSound(notification);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " onReceive :: ", e);
        }
    }

    /**
     * Wakeup the device.
     *
     * @param context The application context.
     */
    private void wakeUp(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);

        if (pm == null)
            return;

        int level = PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP;

        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                level, "awqat:myWakeLock");

        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(1000);

        if (SDK_INT >= LOLLIPOP) {
            wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);
        } else {
            wakeLock.release();
        }
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, PauseSoundReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
