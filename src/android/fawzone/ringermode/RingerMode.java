package org.fawzone.ringermode;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
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

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

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


    public static void displayNotification(Context context, String channelId, String icId, String name, String title, String text ){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// 1. Créer le canal de notification (indispensable sur Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    name,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setSound(null, null);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }

// 2. Charger la mise en page personnalisée (avec récupération dynamique pour plugin)
        String packageName = context.getPackageName();
        int layoutId = context.getResources().getIdentifier("custom_ringer_notif_small", "layout", packageName);
        int layoutId2 = context.getResources().getIdentifier("custom_ringer_notif_big", "layout", packageName);
        int textId = context.getResources().getIdentifier("notif_text", "id", packageName);
        int iconId = context.getResources().getIdentifier("notif_icon", "id", packageName);
        int drawableIcon = context.getResources().getIdentifier(icId, "drawable", packageName);

        RemoteViews customView = new RemoteViews(packageName, layoutId);
        customView.setTextViewText(textId, text);
        customView.setImageViewResource(iconId, drawableIcon);

// 3. Construire la notification
/*       NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
               .setSmallIcon(drawableIcon) // Icône obligatoire dans la barre d'état
               .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
               .setCustomContentView(customView)
               .setPriority(NotificationCompat.PRIORITY_HIGH)
               .setSilent(true)
               .setTimeoutAfter(30000)
               .setAutoCancel(true);*/

        RemoteViews compactView = new RemoteViews(packageName, layoutId);
        compactView.setTextViewText(textId, text);
        compactView.setImageViewResource(iconId, drawableIcon);

        RemoteViews bigView = new RemoteViews(packageName, layoutId2);
        bigView.setTextViewText(textId, text);
        bigView.setImageViewResource(iconId, drawableIcon);

        // 1. Convertir votre icône vectorielle ou drawable en Bitmap
        Drawable drawable = ContextCompat.getDrawable(context, drawableIcon);
        assert drawable != null;
        Bitmap largeBitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(largeBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

// 2. Construire la notification standard avec la grande image
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(drawableIcon) // Petit badge en haut à gauche + barre d'état
                .setLargeIcon(largeBitmap)   // Grande image affichée bien visible à droite !
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSilent(true)
                .setTimeoutAfter(30000)
                .setAutoCancel(true);

// 4. L'afficher immédiatement (1001 est l'ID unique de la notification)
        if (notificationManager != null) {
            notificationManager.notify(10610, builder.build());
        }

    }


}
