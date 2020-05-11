package org.fawzone.sound;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.appplant.cordova.plugin.notification.Notification;

import static de.appplant.cordova.plugin.localnotification.LocalNotification.fireEvent;
import static de.appplant.cordova.plugin.localnotification.LocalNotification.isAppRunning;

public class SoundManager {

    public static final String TAG = "SoundManager";

    private static MediaPlayer sound;

    public static void createSound(Uri soundUri, Context context, Notification notification) {

        if (sound != null) {
            try {
                Log.w(TAG, "    createSound OLD SOUND IS NOT NULL ==> release ...");
                sound.release();
            } catch(Exception e){
                Log.e(TAG, " createSound :: old sound.release() ", e);
            }
        }

        try {
            sound = MediaPlayer.create(context, soundUri);
     // TODO   addSoundActions(builder, extras);

            sound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i ("TAG", notification.getId()+"  :  sound complete clearing notification  ");
                    notification.clear();
                }
            });

        } catch(Exception e){
            Log.e(TAG, " createSound :: old sound.release() ", e);
        }

    }


    private static Integer getNotificationId(Notification notification){
        return notification!=null?notification.getId():null;
    }

    private static boolean isMustFireEvent(Notification notification){
        return notification!=null && isAppRunning();
    }

    public static void playSound(Notification notification){
        Integer id =getNotificationId(notification);

        Log.i(TAG," > playSound  "+id);

        if(sound == null){
            Log.w(TAG,"    playSound  "+id+"  SOUND IS NULL ");
            return;
        }

        try {
            if(sound.isPlaying()){
                Log.w(TAG,"    playSound  "+id+"  SOUND IS PLAYING ");
                return;
            }

            sound.start();

            if (isMustFireEvent( notification)) {
                fireEvent("playSound", notification);
            }

        } catch(Exception e){
            Log.e(TAG, " playSound :: " , e);
        }
    }

    public static void stopSound(Notification notification){
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

            if (isMustFireEvent( notification)) {
                fireEvent("stopSound", notification);
            }
//TODO             sound.release();
        } catch(Exception e){
            Log.e(TAG, " stopSound :: " , e);
        }
    }

    public static void pauseSound(Notification notification){
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

            if (isMustFireEvent( notification)) {
                fireEvent("pauseSound", notification);
            }
        } catch(Exception e){
            Log.e(TAG, " pauseSound :: " , e);
        }
    }

    public static void resumeSound(Notification notification){
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

            if (isMustFireEvent(notification)) {
                fireEvent("resumeSound", notification);
            }

        } catch(Exception e){
            Log.e(TAG, " resumeSound :: " , e);
        }
    }






}