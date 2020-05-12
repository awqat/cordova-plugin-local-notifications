package org.fawzone.sound;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import de.appplant.cordova.plugin.notification.Notification;

import static de.appplant.cordova.plugin.localnotification.LocalNotification.fireEvent;
import static de.appplant.cordova.plugin.localnotification.LocalNotification.isAppRunning;

public class SoundManager {

    public static final String TAG = "SoundManager";

    public static final String PAUSE_SOUND = "pauseSound";
    public static final String RESUME_SOUND = "resumeSound";
    public static final String PLAY_SOUND = "playSound";
    public static final String STOP_SOUND = "stopSound";
    public static final String SOUND_COMPLETE = "soundComplete";

    private static MediaPlayer sound;

    private static Uri soundUri;

    public static void createSound(Uri aSoundUri, Context context, Notification notification) {

        if (sound != null) {
            try {
                Log.w(TAG, "    createSound OLD SOUND IS NOT NULL ==> release ...");
                sound.release();
            } catch(Exception e){
                Log.e(TAG, " createSound :: old sound.release() ", e);
            }
        }

        try {
            sound = MediaPlayer.create(context, aSoundUri);
     // TODO   addSoundActions(builder, extras);

            sound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i ("TAG", notification.getId()+"  :  sound complete clearing notification  ");
                    notification.clear();

                    sound = null;
                    soundUri = null;

                    if (canFireEvent( notification)) {
                        fireEvent(SOUND_COMPLETE, notification);
                    }
                }
            });

        } catch(Exception e){
            Log.e(TAG, " createSound :: old sound.release() ", e);
        }

    }


    private static Integer getNotificationId(Notification notification){
        return notification!=null?notification.getId():null;
    }

    private static boolean canFireEvent(Notification notification){
        return notification!=null && isAppRunning();
    }

    public static boolean hasNotificationNoSound(Notification notification){
        return notification != null && notification.getOptions().getSound() == null;
    }

    public static void playSound(Notification notification, boolean fireEvent){
        Integer id =getNotificationId(notification);
        Log.i(TAG," > playSound  "+id);

        if(sound == null){
            Log.w(TAG,"    playSound  "+id+"  SOUND IS NULL ");
            return;
        }

        if(hasNotificationNoSound(notification)){
            Log.w(TAG,"    playSound  current notifiaction "+notification.getId()+" HAS NO SOUND ");
            return;
        }

        try {
            if(sound.isPlaying()){
                Log.w(TAG,"    playSound  "+id+"  SOUND IS PLAYING ");
                return;
            }

            sound.start();

            if (fireEvent && canFireEvent( notification)) {
                fireEvent(PLAY_SOUND, notification);
            }


        } catch(Exception e){
            Log.e(TAG, " playSound :: " , e);
        }
    }



    public static void stopSound(Notification notification, boolean fireEvent){
        Integer id =getNotificationId(notification);

        Log.i(TAG," > stopSound  "+id);

        if(sound == null){
            Log.w(TAG,"    stopSound  "+id+"  SOUND IS NULL ");
            return;
        }

        try {
            if(!sound.isPlaying()){
                Log.w(TAG,"    stopSound  "+id+"  SOUND IS NOT PLAYING ");
                return;
            }

            sound.stop();

            if (fireEvent && canFireEvent( notification)) {
                fireEvent(STOP_SOUND, notification);
            }
//TODO             sound.release();
        } catch(Exception e){
            Log.e(TAG, " stopSound :: " , e);
        }
    }

    public static void pauseSound(Notification notification, boolean fireEvent){
        Integer id =getNotificationId(notification);

        Log.i(TAG," > pauseSound  "+id);

        if(sound == null){
            Log.w(TAG,"    pauseSound  "+id+"  SOUND IS NULL ");
            return;
        }

        try {
            if(!sound.isPlaying()){
                Log.w(TAG,"    pauseSound  "+id+"  SOUND IS NOT PLAYING ");
                return;
            }
            sound.pause();

            if (fireEvent && canFireEvent( notification)) {
                fireEvent(PAUSE_SOUND, notification);
            }
        } catch(Exception e){
            Log.e(TAG, " pauseSound :: " , e);
        }
    }

    public static void resumeSound(Notification notification, boolean fireEvent){
        Integer id =getNotificationId(notification);

        Log.i(TAG," > stopSound  "+id);

        if(sound == null){
            Log.w(TAG,"    stopSound  "+id+"  SOUND IS NULL ");
            return;
        }

        try {
            if(sound.isPlaying()){
                Log.w(TAG,"    stopSound  "+id+"  SOUND IS PLAYING ");
                return;
            }

            sound.seekTo(sound.getCurrentPosition());
            sound.start();

            if (fireEvent && canFireEvent( notification)) {
                fireEvent(RESUME_SOUND, notification);
            }

        } catch(Exception e){
            Log.e(TAG, " resumeSound :: " , e);
        }
    }



}