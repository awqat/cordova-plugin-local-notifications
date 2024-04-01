package org.fawzone.util;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static android.app.PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;


import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.fawzone.sound.StopSoundReceiver;
import org.json.JSONObject;

import de.appplant.cordova.plugin.localnotification.LocalNotification;
import de.appplant.cordova.plugin.localnotification.TriggerReceiver;
import de.appplant.cordova.plugin.notification.Manager;
import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.util.AssetUtil;

public class Utils {

    private static final String TAG = "UTILS";

    public static void updateNotification(JSONObject update, Notification notification) {
        Manager manager =Manager.getInstance(notification.getContext());
        manager.update(notification.getId(), update, TriggerReceiver.class);
    }


    public static Integer getNotificationId(Notification notification) {
        return notification != null ? ( notification.getOptions()!=null ? notification.getId(): null) : null;
    }

    public static boolean canFireEvent(Notification notification) {
        return notification != null && notification.getOptions() != null;
    }

    public static boolean hasNotificationNoSound(Notification notification) {
        return notification != null && notification.getOptions() != null && (notification.getOptions().getSound() == Uri.EMPTY || notification.getOptions().getSound() == null);
    }

    public static boolean hasNotificationVibration(Notification notification) {
        return notification != null && notification.getOptions() != null && (notification.getOptions().isWithVibration());
    }


    public static void displayToast(Context context, String text, String imageUri,int duration){

        AssetUtil assets = AssetUtil.getInstance(context);

        // create a handler to post messages to the main thread
        Handler mHandler = new Handler(context.getMainLooper());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, text, duration); // For example

                // toast.setMargin(10,10);
                // toast.setGravity(Gravity.CENTER, 10, 10);
                LinearLayout toastContentView = (LinearLayout) toast.getView();
                
                if(toastContentView != null){
                    ImageView imageView = new ImageView(context);
                    //imageView.setImageBitmap(options.getRingerModeVibrateIconNormal());

                    imageView.setImageURI(assets.parse(imageUri));
                    //imageView.setBackgroundColor(Color.GRAY);

                    toastContentView.addView(imageView, 0);
                }
                toast.show();
            }
        });
    }

    /**
     *
     * @param context
     * @param flagsIn
     *
     * @return
     */
    public static int buildPendingIntentFlags(Context context, int flagsIn){
        int flagsOut = 0;

        if (SDK_INT >= S && SDK_INT < UPSIDE_DOWN_CAKE  ) {
            flagsOut = flagsIn | FLAG_MUTABLE;
        } else if (SDK_INT >= UPSIDE_DOWN_CAKE) {
            flagsOut = flagsIn | FLAG_MUTABLE | FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT;
        } else {
            flagsOut = flagsIn;
        }
        return flagsOut;
    }

    /**
     * Build Trampoline Activity Intent
     *
     * @param context context
     * @param intent intent
     * @param reqCode req code
     *
     * @return pending intent
     */
    public static PendingIntent buildTrampolineActivityIntent(Context context, Intent intent, int reqCode, int flags){

        //If < Android 12
        if( SDK_INT < S || LocalNotification.isAppRunning()){
            PendingIntent contentIntent = PendingIntent.getService(
                             context, reqCode, intent, flags);
            return contentIntent;
        }

        //Else

        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();

        /*
        Intent intent0 = new Intent(context, StopSoundReceiver.class);
        intent0.setComponent(new ComponentName(packageName, className));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
       // stackBuilder.addNextIntent(intent);
        stackBuilder.addNextIntent(intent0);
*/
        //Intent intent = new Intent(context, StopSoundReceiver.class);
        intent.setComponent(new ComponentName(packageName, className));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // stackBuilder.addNextIntent(intent);
        stackBuilder.addNextIntent(intent);



//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(reqCode, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(reqCode, flags);
        return resultPendingIntent;

    }

}