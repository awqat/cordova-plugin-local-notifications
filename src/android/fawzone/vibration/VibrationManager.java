package org.fawzone.vibration;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import org.fawzone.sound.SoundManager;
import org.fawzone.util.Utils;

import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.util.AssetUtil;

import static de.appplant.cordova.plugin.localnotification.LocalNotification.fireEvent;

public class VibrationManager {

    public static final String TAG = "VibrationManager";

    public static final String PLAY_VIBRATION = "playVibration";
    public static final String STOP_VIBRATION = "stopVibration";
    public static final long[] M_VIBRATE_PATTERN = {0, 400, 800, 600, 800, 800, 800, 1000, 400, 800, 600, 800, 800, 800, 1000};

    private static Vibrator vibrator;


    public static void createVibration(Context context, Notification notification) {
        Log.i(TAG, " > createVibration  ");


        
        if (vibrator != null) {
            try {
                Log.w(TAG, "    createVibration OLD VIBRATOR IS NOT NULL ==> cancel ...");
                if(vibrator.hasVibrator()){
                    vibrator.cancel();
                }
            } catch (Exception e) {
                Log.e(TAG, " createVibration :: old VIBRATOR.cancel() ", e);
            }
        }
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);


    }



    public static void playVibration(Notification notification, boolean fireEvent){
        Integer id = Utils.getNotificationId(notification);
        Log.i(TAG, " > playVibration  " + id);



        if (!Utils.hasNotificationVibration(notification)) {
            Log.w(TAG, "    playVibration  current notifiaction" + notification.getId() + " HAS  no vibration  ");
            return;
        }

        if(vibrator == null && notification.getContext()!=null){
            vibrator = (Vibrator) notification.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }

        if(vibrator == null){
            Log.i(TAG, " > playVibration  " + id+ "  vibrator NULL ");
            return;
        }

        try {
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //vibrator.vibrate(VibrationEffect.createOneShot(20000, VibrationEffect.DEFAULT_AMPLITUDE));
                Log.i(TAG, " > playVibration Build.VERSION.SDK_INT >= Build.VERSION_CODES.O  " + id);
                long[] mVibratePattern = M_VIBRATE_PATTERN;
                vibrator.vibrate(VibrationEffect.createWaveform(mVibratePattern , 3 ) );
            } else {
                Log.i(TAG, " > playVibration Build.VERSION.SDK_INT < Build.VERSION_CODES.O  deprecated in API 26 " + id);
                //deprecated in API 26

                long[] mVibratePattern = M_VIBRATE_PATTERN;
                vibrator.vibrate(mVibratePattern, 3);


            }
            if (fireEvent && Utils.canFireEvent(notification)) {
                fireEvent(PLAY_VIBRATION, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, " playVibration :: ", e);
        }


    }


    public static void stopVibration(Notification notification, boolean fireEvent) {
        Integer id = Utils.getNotificationId(notification);

        Log.i(TAG, " > stopVibration  " + id);

        if (vibrator == null) {
            Log.w(TAG, "    stopVibration  " + id + "  VIBRATOR IS NULL ");
            return;
        }

        try {
            if (!vibrator.hasVibrator()) {
                Log.w(TAG, "    stopVibration  " + id + "  VIBRATOR IS NOT PLAYING ");
                return;
            }

            vibrator.cancel();

            if (fireEvent && Utils.canFireEvent(notification)) {
                fireEvent(STOP_VIBRATION, notification);
            }

            Utils.displayToast(notification.getContext(), "STOP", "res://ic_action_stop.png" , Toast.LENGTH_SHORT);

        } catch (Exception e) {
            Log.e(TAG, " stopSound :: ", e);
        }
    }


}



