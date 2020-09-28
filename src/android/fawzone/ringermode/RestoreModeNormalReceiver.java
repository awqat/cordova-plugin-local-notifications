package org.fawzone.ringermode;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

import capacitor.android.plugins.R;
import de.appplant.cordova.plugin.localnotification.TriggerReceiver;
import de.appplant.cordova.plugin.notification.Manager;
import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.Options;
import de.appplant.cordova.plugin.notification.util.AssetUtil;

import static android.content.Context.POWER_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;


public class RestoreModeNormalReceiver extends BroadcastReceiver {
    public static final String TAG = "RestoreModeNormal";

    public static final String MSG_NORMAL_RESTORED = "MSG_NORMAL_RESTORED";
    public static final String ICON_NORMAL_RESTORED = "ICON_NORMAL_RESTORED";

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Log.e(TAG, " onReceive " + intent);

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if(AudioManager.RINGER_MODE_NORMAL == audioManager.getRingerMode()){
                Log.e(TAG, " onReceive RINGER_MODE_NORMAL ==> do nothig " + intent);
                return;
            }


            wakeUp(context);

            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }

            final String msgRingerModeNormalRestored = bundle.getString(MSG_NORMAL_RESTORED);
            final String iconRingerModeNormal = bundle.getString(ICON_NORMAL_RESTORED);;


            int toastId = bundle.getInt(Notification.EXTRA_ID, 0);

            Log.i(TAG, " toastId :  " + toastId);

           //  Options options = Manager.getInstance(context).getOptions(toastId);
            AssetUtil assets = AssetUtil.getInstance(context);

          //  Log.i(TAG, " options :  " + options);

           /* if (options == null) {
                return;
            }*/

            Log.i(TAG, msgRingerModeNormalRestored);

            // Put here YOUR code.
            //Toast.makeText(context, options.getRingerModeVibrateMsgNormalRestored(), Toast.LENGTH_LONG).show(); // For example



            Log.e(TAG, " onReceive " + intent);



            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Log.e(TAG, " onReceive " + intent);


            Toast toast = Toast.makeText(context, msgRingerModeNormalRestored, Toast.LENGTH_LONG); // For example

            // toast.setMargin(10,10);
            toast.setGravity(Gravity.CENTER, 10, 10);
            LinearLayout toastContentView = (LinearLayout) toast.getView();
            ImageView imageView = new ImageView(context);
            //imageView.setImageBitmap(options.getRingerModeVibrateIconNormal());
            imageView.setImageURI(assets.parse(iconRingerModeNormal));
            //imageView.setBackgroundColor(Color.GRAY);

            toastContentView.addView(imageView, 0);
            toast.show();


            Manager manager =Manager.getInstance(context);

            JSONObject update  = new JSONObject();
            update.put("icon", iconRingerModeNormal);
            update.put("text", msgRingerModeNormalRestored);

            manager.update(toastId, update, TriggerReceiver.class);


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
        Intent intent = new Intent(context, RestoreModeNormalReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
