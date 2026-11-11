package org.fawzone.ringermode;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Calendar;

import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.Options;
import de.appplant.cordova.plugin.notification.Request;
import de.appplant.cordova.plugin.notification.util.AssetUtil;

import static android.app.AlarmManager.RTC;
import static android.app.AlarmManager.RTC_WAKEUP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

import org.fawzone.util.Utils;

public class RingerMode {

    public static final String TAG = "RingerMode";
    public static final int REQUEST_CODE = 234324243;

    public static final String NOTIFICATION = "NOTIFICATION";

    public static void setRingerModeVibrate(Notification notification) {
        try {
            Context context = notification.getContext();
            Options options = notification.getOptions();


            Log.i(TAG, "  RingerModeVibrateEnable =  "+options.getRingerModeVibrateEnable());

            if (!options.getRingerModeVibrateEnable()) {
                Log.i(TAG, " ! RingerModeVibrateEnable => do nothing  ");
                return;
            }



            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


            if (AudioManager.RINGER_MODE_NORMAL != audioManager.getRingerMode()) {

                Log.i(TAG, " !RINGER_MODE_NORMAL => do nothing  ");

                return;
            }
            // ==== Begin Mode vibrate

            try {
                Intent serviceIntent = new Intent(context, VibrateForegroundService.class);
                serviceIntent.setAction(VibrateForegroundService.ACTION_SET_VIBRATE);
                if (SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
                Log.i(TAG, "VibrateForegroundService démarré pour RINGER_MODE_VIBRATE !");
            } catch (Exception e) {
                Log.e(TAG, "Erreur démarrage service Vibrate", e);
                try {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                } catch (Exception ex) {
                    Log.e(TAG, "Fallback setRingerMode error", ex);
                }
            }

            AssetUtil assets = AssetUtil.getInstance(options.getContext());
            int vibrateResId = assets.getResId("res://ic_action_vibrate.png");
            if (vibrateResId != 0) {
                notification.setSmallIcon(vibrateResId);
            } else {
                int alarmResId = assets.getResId("res://ic_action_alarm.png");
                if (alarmResId != 0) {
                    notification.setSmallIcon(alarmResId);
                }
            }

            Log.e(TAG, "  schedule retore ringer mode normal ... ");

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, RestoreModeNormalReceiver.class)
                    .putExtra(Notification.EXTRA_ID, options.getId())
                    .putExtra(RestoreModeNormalReceiver.ICON_NORMAL_RESTORED, options.getRingerModeVibrateIconNormalName())
                    .putExtra(RestoreModeNormalReceiver.MSG_NORMAL_RESTORED, options.getRingerModeVibrateMsgNormalRestored());

            //Build flags
            int flags = Utils.buildPendingIntentFlags(context, PendingIntent.FLAG_CANCEL_CURRENT);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags);

            int restoreModeNormalAfter = options.getRingerModeVibrateRestoreAfter(); // 60 * 30 * 1000;

            // Calculate time until alarm from millis since epoch
            Calendar calendar = Calendar.getInstance();
            Log.i(TAG, " calendarTime : " + calendar.getTime().getTime());

            calendar.add(Calendar.MILLISECOND, restoreModeNormalAfter);
            long triggerTime = calendar.getTimeInMillis();

            Log.i(TAG, " triggerTime  : " + triggerTime);

            if (SDK_INT >= M) {
                alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setExact(RTC_WAKEUP, triggerTime, pendingIntent);
            }

            if (options.getRingerModeVibrateMsgVibrateEnabled() != null && !options.getRingerModeVibrateMsgVibrateEnabled().isEmpty()) {
                Toast.makeText(context.getApplicationContext(), options.getRingerModeVibrateMsgVibrateEnabled(), Toast.LENGTH_LONG).show();
            }

/*
            Toast toast = new Toast(context);
            View toast_view = LayoutInflater.from(context).inflate(R.layout.support_simple_spinner_dropdown_item, null);
            toast.setView(toast_view);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
*/
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " setRingerModeVibrate :: ", e);
        }
        // ==== End   Mode vibrate

    }

}
