package org.fawzone.sound;

import android.media.MediaPlayer;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    public static final String TAG = "SoundManager";

    private static final Map<Integer, MediaPlayer> SOUNDS_MAP = new HashMap<>();

    public static void putSound(Integer id, MediaPlayer sound){
        Log.i(TAG," > putSound  "+id);
        MediaPlayer soundOld = getSound(id);

        if(soundOld != null){
            Log.w(TAG,"    putSound  "+id+" OLD SOUND IS NOT NULL ==> release ...");
            soundOld.release();
        }

        SOUNDS_MAP.put(id, sound);
    }

    public static MediaPlayer getSound(Integer id){
        return SOUNDS_MAP.get(id);
    }

    public static void playSound(Integer id){
        Log.i(TAG," > playSound  "+id);
        MediaPlayer sound = getSound(id);

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
        } catch(Exception e){
            Log.e(TAG, " playSound :: " , e);
        }
    }

    public static void stopSound(Integer id){
        Log.i(TAG," > stopSound  "+id);

        MediaPlayer sound = getSound(id);

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
        } catch(Exception e){
            Log.e(TAG, " stopSound :: " , e);
        }
    }

    public static void pauseSound(Integer id){
        Log.i(TAG," > pauseSound  "+id);

        MediaPlayer sound = getSound(id);

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
        } catch(Exception e){
            Log.e(TAG, " pauseSound :: " , e);
        }
    }

    public static void resumeSound(Integer id){
        Log.i(TAG," > stopSound  "+id);

        MediaPlayer sound = getSound(id);

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
        } catch(Exception e){
            Log.e(TAG, " resumeSound :: " , e);
        }
    }


    public static void stopSounds(){
        for (Map.Entry<Integer,MediaPlayer> entry :  SOUNDS_MAP.entrySet()){
            Log.i(TAG, "   Stop :  "+entry.getKey()  );

            try {

                if (!entry.getValue().isPlaying()) {
                    continue;
                }

                entry.getValue().stop();
                entry.getValue().release();

            } catch(Exception e){
                Log.e(TAG, " stopSounds :: " , e);
            }

        }
    }



}