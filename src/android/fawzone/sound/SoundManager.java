package org.fawzone.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.media.AudioPlayer;

import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.util.AssetUtil;

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

    private static final String SOUND_FAILBACK_FILE = "public/audio/allahu_akbar.mp3";

    public static class SoundOnCompletionListener implements MediaPlayer.OnCompletionListener {

        private Notification notification;

        public SoundOnCompletionListener(Notification notification){
            this.notification = notification;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.i("TAG", notification.getId() + "  :  sound complete clearing notification  ");
            notification.clear();

            try {
                sound.release();
            } catch (Exception e) {
                Log.e(TAG, " playSound :: onCompletion :: sound.release ", e);
            }

            sound = null;

            if (canFireEvent(notification)) {
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
            sound.setOnCompletionListener(new SoundOnCompletionListener(notification));
            // seek to any location received while not prepared
            //this.seekToPlaying(this.seekOnPrepared);
            // If start playing after prepared
            //if (!this.prepareOnly) {
            sound.start();
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
            sound.prepareAsync();


            // TODO   addSoundActions(builder, extras);

        /*    sound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i("TAG", notification.getId() + "  :  sound complete clearing notification  ");
                    notification.clear();

                    try {
                        sound.release();
                    } catch (Exception e) {
                        Log.e(TAG, " playSound :: onCompletion :: sound.release ", e);
                    }

                    sound = null;

                    if (canFireEvent(notification)) {
                        fireEvent(SOUND_COMPLETE, notification);
                    }
                }
            });
*/
        } catch (Exception e) {
            Log.e(TAG, " createSound :: old sound.release() ", e);
        }

    }


    private static Integer getNotificationId(Notification notification) {
        return notification != null ? notification.getId() : null;
    }

    private static boolean canFireEvent(Notification notification) {
        return notification != null;
    }

    public static boolean hasNotificationNoSound(Notification notification) {
        return notification != null && notification.getOptions().getSound() == null;
    }

    public static void playSound(Notification notification, boolean fireEvent) {
        Integer id = getNotificationId(notification);
        Log.i(TAG, " > playSound  " + id);



        if (sound == null) {
            Log.w(TAG, "    playSound  " + id + "  SOUND IS NULL ");
            return;
        }



        if (hasNotificationNoSound(notification)) {
            Log.w(TAG, "    playSound  current notifiaction " + notification.getId() + " HAS NO SOUND ");
            return;
        }

        try {
            if (sound.isPlaying()) {
                Log.w(TAG, "    playSound  " + id + "  SOUND IS PLAYING ");
                return;
            }

            sound.start();

            if (fireEvent && canFireEvent(notification)) {
                fireEvent(PLAY_SOUND, notification);
            }


        } catch (Exception e) {
            Log.e(TAG, " playSound :: ", e);
        }
    }


    public static void stopSound(Notification notification, boolean fireEvent) {
        Integer id = getNotificationId(notification);

        Log.i(TAG, " > stopSound  " + id);

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

            if (fireEvent && canFireEvent(notification)) {
                fireEvent(STOP_SOUND, notification);
            }
//TODO             sound.release();
        } catch (Exception e) {
            Log.e(TAG, " stopSound :: ", e);
        }
    }

    public static void pauseSound(Notification notification, boolean fireEvent) {
        Integer id = getNotificationId(notification);

        Log.i(TAG, " > pauseSound  " + id);

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

            if (fireEvent && canFireEvent(notification)) {
                fireEvent(PAUSE_SOUND, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, " pauseSound :: ", e);
        }
    }

    public static void resumeSound(Notification notification, boolean fireEvent) {
        Integer id = getNotificationId(notification);

        Log.i(TAG, " > stopSound  " + id);

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

            if (fireEvent && canFireEvent(notification)) {
                fireEvent(RESUME_SOUND, notification);
            }

        } catch (Exception e) {
            Log.e(TAG, " resumeSound :: ", e);
        }
    }


}