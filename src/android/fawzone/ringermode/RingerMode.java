package org.fawzone.ringermode;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.util.Log;
import android.view.Gravity;
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

public class RingerMode {

    public static final String TAG = "RingerMode";
    public static final int REQUEST_CODE = 234324243;

    public static final String NOTIFICATION = "NOTIFICATION";

    public static void setRingerModeVibrate(Notification notification) {
        try {
            Context context = notification.getContext();
            Options options = notification.getOptions();

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

            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

            notification.setLargeIcon(options.getRingerModeVibrateIconVibrate());


            Log.e(TAG, "  schedule retore ringer mode normal ... ");

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            //Intent intent = new Intent(context, RestoreModeNormalReceiver.class);

            Intent intent = new Intent(context, RestoreModeNormalReceiver.class)
                    .putExtra(Notification.EXTRA_ID, options.getId())
                    .putExtra(RestoreModeNormalReceiver.ICON_NORMAL_RESTORED, options.getRingerModeVibrateIconNormalName())
                    .putExtra(RestoreModeNormalReceiver.MSG_NORMAL_RESTORED, options.getRingerModeVibrateMsgNormalRestored());


            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

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

            // Put here YOUR code.
            Toast toast = Toast.makeText(context, options.getRingerModeVibrateMsgVibrateEnabled(), Toast.LENGTH_LONG); // For example

            // toast.setMargin(10,10);

            toast.setGravity(Gravity.CENTER, 10, 10);
            LinearLayout toastContentView = (LinearLayout) toast.getView();
            //toastContentView.setDividerPadding(10);


            //toastContentView.setBackgroundColor(Color.WHITE);

            ImageView imageView = new ImageView(context);
            //imageView.setImageBitmap(options.getRingerModeVibrateIconVibrate());
            AssetUtil assets = AssetUtil.getInstance(context);
            imageView.setImageURI(assets.parse(options.getRingerModeVibrateIconVibrateName()));
            //imageView.setBackgroundColor(Color.WHITE);

            toastContentView.addView(imageView, 0);
            toast.show();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " setRingerModeVibrate :: ", e);
        }
        // ==== End   Mode vibrate

    }

}
