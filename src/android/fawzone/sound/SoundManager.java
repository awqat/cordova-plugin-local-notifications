package org.fawzone.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


import org.apache.cordova.LOG;
import org.fawzone.util.Utils;
import org.fawzone.vibration.VibrationManager;
import org.json.JSONObject;

import de.appplant.cordova.plugin.localnotification.TriggerReceiver;
import de.appplant.cordova.plugin.notification.Manager;
import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.Options;
import de.appplant.cordova.plugin.notification.util.AssetUtil;

import static de.appplant.cordova.plugin.localnotification.LocalNotification.fireEvent;

public class SoundManager {

    public static final String TAG = "SoundManager";

    public static final String PAUSE_SOUND = "pauseSound";
    public static final String RESUME_SOUND = "resumeSound";
    public static final String PLAY_SOUND = "playSound";
    public static final String STOP_SOUND = "stopSound";
    public static final String SOUND_COMPLETE = "soundComplete";

    private static MediaPlayer sound;

    private static final String SOUND_FAILBACK_FILE = "public/audio/chant_rossignol.mp3";

    public static class SoundOnCompletionListener implements MediaPlayer.OnCompletionListener {

        private Notification notification;

        public SoundOnCompletionListener(Notification notification){
            this.notification = notification;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, " > SoundOnPreparedListener.onCompletion  "+notification.getId() );

            try {

                JSONObject update  = new JSONObject();
                update.put(Options.OPT_SOUND, null);
                update.put(Options.OPT_SOUND_DETACHED, false);

                Utils.updateNotification(update, notification);

                sound.release();
            } catch (Exception e) {
                Log.e(TAG, " playSound :: onCompletion :: sound.release ", e);
            }

            sound = null;

            if (Utils.canFireEvent(notification)) {
                fireEvent(SOUND_COMPLETE, notification);
            }
        }


    }


    public static class SoundOnPreparedListener implements MediaPlayer.OnPreparedListener {

        private Notification notification;

        public SoundOnPreparedListener(Notification notification){
            this.notification = notification;
        }
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.i(TAG, " > SoundOnPreparedListener.onPrepared  " );
            sound.setOnCompletionListener(new SoundOnCompletionListener(notification));
            // seek to any location received while not prepared
            //this.seekToPlaying(this.seekOnPrepared);
            // If start playing after prepared
            //if (!this.prepareOnly) {
            //sound.start();

            playSound(notification,true);
            //this.setState(STATE.MEDIA_RUNNING);
            //this.seekOnPrepared = 0; //reset only when played
            //} else {
            //  this.setState(STATE.MEDIA_STARTING);
            // }
            // Save off duration
            //    this.duration = getDurationInSeconds();
            // reset prepare only flag
            //  this.prepareOnly = true;

        }
    }


    public static class SoundOnErrorListener implements MediaPlayer.OnErrorListener {

        private Notification notification;

        private Context context;

        public SoundOnErrorListener(Notification notification, Context context){
            this.notification = notification;
            this.context = context;
        }


        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, " SoundOnErrorListener.onError  what = "+what+", extra = "+extra + " --> failback to  "+SOUND_FAILBACK_FILE);

            try {
                sound = MediaPlayer.create(context, AssetUtil.getInstance(context).parse(SOUND_FAILBACK_FILE));
                sound.setOnCompletionListener(new SoundOnCompletionListener(notification));
                sound.start();
            } catch(Exception e){
                Log.e(TAG, " SoundOnErrorListener.onError :: ",e );
                return false;
            }
            return true;
        }
    }


    public static void createSound(Uri aSoundUri, Context context, Notification notification) {
        Log.i(TAG, " > createSound  "+aSoundUri);

        if (sound != null) {
            try {
                Log.w(TAG, "    createSound OLD SOUND IS NOT NULL ==> release ...");
                if(sound.isPlaying()){
                    sound.stop();


                }
                sound.release();
            } catch (Exception e) {
                Log.e(TAG, " createSound :: old sound.release() ", e);
            }
        }

        try {


            if(AssetUtil.isStreaming(aSoundUri)){
                sound = new MediaPlayer();
                sound.setDataSource(aSoundUri.getPath());
                //   sound.setAudioStreamType(AudioManager.STREAM_MUSIC);
                sound.setAudioAttributes(new
                        AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
            } else {
                sound = MediaPlayer.create(context, aSoundUri);
            }

            //if it's a streaming file, play mode is implied
            //this.setMode(AudioPlayer.MODE.PLAY);
            //this.setState(AudioPlayer.STATE.MEDIA_STARTING);
            sound.setOnPreparedListener(new SoundOnPreparedListener(notification));
            sound.setOnErrorListener(new SoundOnErrorListener(notification, context));

        } catch (Exception e) {
            Log.e(TAG, " createSound :: old sound.release() ", e);
        }

    }


    public static void playSoundOnReady(Notification notification, boolean fireEvent) {
        Integer id = Utils.getNotificationId(notification);
        Log.i(TAG, " > playSoundOnReady  "+id);

        VibrationManager.playVibration(notification, fireEvent);

        if (sound == null) {
            Log.w(TAG, "    playSoundOnReady  " + id + "  SOUND IS NULL ");
            return;
        }

        if (Utils.hasNotificationNoSound(notification)) {
            Log.w(TAG, "    playSoundOnReady  current notifiaction " + notification.getId() + " HAS NO SOUND ");
            return;
        }

        try {
            if (sound.isPlaying()) {
                Log.w(TAG, "    playSoundOnReady  " + id + "  SOUND IS PLAYING ");
                return;
            }

            sound.prepareAsync();

            if (fireEvent && Utils.canFireEvent(notification)) {
                fireEvent(PLAY_SOUND, notification);
            }

            setSoundVolume(notification);

        } catch (Exception e) {
            Log.e(TAG, " playSoundOnReady :: ", e);
        }


    }

    private static void setSoundVolume(Notification notification) {
        final AudioManager manager = (AudioManager)notification.getContext().getSystemService(Context.AUDIO_SERVICE);
        final int soundVolume = notification.getOptions().getSoundVolume();

        Log.i(TAG, "    setSoundVolume notifiaction " + notification.getId() + " soundVolume "+soundVolume);

        if(soundVolume<=0){
            return;
        }

        final int volumeToSet = getVolumeToSet(manager, soundVolume);
        Log.i(TAG, "    setSoundVolume notifiaction " + notification.getId() + " volumeToSet "+volumeToSet);
        //sound.setVolume(volumeToSet, volumeToSet);
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeToSet,  AudioManager.FLAG_PLAY_SOUND );
    }


    private static int getVolumeToSet(AudioManager manager, int percent) {
        try {
            int volLevel;
            int maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            volLevel = Math.round((percent * maxVolume) / 100);

            return volLevel;
        } catch (Exception e){
            LOG.d(TAG, "Error getting VolumeToSet: " + e);
            return 1;
        }
    }

    public static void playSound(Notification notification, boolean fireEvent) {
        Integer id = Utils.getNotificationId(notification);
       Log.i(TAG, " > playSound  " + id);



        if (sound == null) {
            Log.w(TAG, "    playSound  " + id + "  SOUND IS NULL ");
            return;
        }

        if (Utils.hasNotificationNoSound(notification)) {
            Log.w(TAG, "    playSound  current notifiaction " + notification.getId() + " HAS NO SOUND ");
            return;
        }

        setSoundVolume(notification);
        try {
            if (sound.isPlaying()) {
                Log.w(TAG, "    playSound  " + id + "  SOUND IS PLAYING ");
                return;
            }

            sound.start();

            if (fireEvent && Utils.canFireEvent(notification)) {
                fireEvent(PLAY_SOUND, notification);
            }


        } catch (Exception e) {
            Log.e(TAG, " playSound :: ", e);
        }
    }




    public static void stopSound(Notification notification, boolean fireEvent) {
        Integer id = Utils.getNotificationId(notification);

        Log.i(TAG, " > stopSound  " + id);

        VibrationManager.stopVibration(notification, fireEvent);

        if (sound == null) {
            Log.w(TAG, "    stopSound  " + id + "  SOUND IS NULL ");
            return;
        }

        try {
            if (!sound.isPlaying()) {
                Log.w(TAG, "    stopSound  " + id + "  SOUND IS NOT PLAYING ");
                return;
            }

            sound.stop();

            if (fireEvent && Utils.canFireEvent(notification)) {
                fireEvent(STOP_SOUND, notification);
            }

            Utils.displayToast(notification.getContext(), "STOP", "res://ic_action_stop.png" , Toast.LENGTH_SHORT);

//TODO             sound.release();
        } catch (Exception e) {
            Log.e(TAG, " stopSound :: ", e);
        }
    }

    public static void pauseSound(Notification notification, boolean fireEvent) {
        Integer id = Utils.getNotificationId(notification);

        Log.i(TAG, " > pauseSound  " + id);

        VibrationManager.stopVibration(notification, fireEvent);

        if (sound == null) {
            Log.w(TAG, "    pauseSound  " + id + "  SOUND IS NULL ");
            return;
        }

        try {
            if (!sound.isPlaying()) {
                Log.w(TAG, "    pauseSound  " + id + "  SOUND IS NOT PLAYING ");
                return;
            }
            sound.pause();


          //  Toast toast0 = Toast.makeText(notification.getContext(), "PausedEEEEEEE", Toast.LENGTH_LONG); // For example
          //  toast0.show();

            if (fireEvent && Utils.canFireEvent(notification)) {
                fireEvent(PAUSE_SOUND, notification);
            }

            Utils.displayToast(notification.getContext(), "PAUSE", "res://ic_action_pause.png" , Toast.LENGTH_SHORT);

        } catch (Exception e) {
            Log.e(TAG, " pauseSound :: ", e);
        }
    }




    public static void resumeSound(Notification notification, boolean fireEvent) {
        Integer id = Utils.getNotificationId(notification);

        Log.i(TAG, " > stopSound  " + id);

        VibrationManager.stopVibration(notification, fireEvent);

        if (sound == null) {
            Log.w(TAG, "    stopSound  " + id + "  SOUND IS NULL ");
            return;
        }

        try {
            if (sound.isPlaying()) {
                Log.w(TAG, "    stopSound  " + id + "  SOUND IS PLAYING ");
                return;
            }

            sound.seekTo(sound.getCurrentPosition());
            sound.start();


            if (fireEvent && Utils.canFireEvent(notification)) {
                fireEvent(RESUME_SOUND, notification);
            }

            Utils.displayToast(notification.getContext(), "PLAY", "res://ic_action_play.png" , Toast.LENGTH_SHORT);



        } catch (Exception e) {
            Log.e(TAG, " resumeSound :: ", e);
        }
    }


}

