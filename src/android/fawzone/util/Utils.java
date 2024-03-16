package org.fawzone.util;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

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
                toast.setGravity(Gravity.CENTER, 10, 10);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            flagsOut = flagsIn | PendingIntent.FLAG_MUTABLE;
        } else {
            flagsOut = flagsIn;
        }
        return flagsOut;
    }

}
