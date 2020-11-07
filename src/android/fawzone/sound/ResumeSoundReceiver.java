package org.fawzone.sound;

import android.os.Bundle;

import androidx.core.app.RemoteInput;
import android.util.Log;

import org.fawzone.util.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.Options;
import de.appplant.cordova.plugin.notification.receiver.AbstractClickReceiver;

import static de.appplant.cordova.plugin.localnotification.LocalNotification.fireEvent;
import static de.appplant.cordova.plugin.notification.Options.EXTRA_LAUNCH;

/**
 * The receiver activity is triggered when a notification is clicked by a user.
 * The activity calls the background callback and brings the launch intent
 * up to foreground.
 */
public class ResumeSoundReceiver extends AbstractClickReceiver {
    public static final String TAG = "ResumeSoundReceiver";


    /**
     * Called when local notification was clicked by the user.
     *
     * @param notification Wrapper around the local notification.
     * @param bundle       The bundled extras.
     */
    @Override
    public void onClick(Notification notification, Bundle bundle) {
        try {

            String action = getAction();
            JSONObject data = new JSONObject();
            Log.e(TAG, " > onClick " + action);
            setTextInput(action, data);
            launchAppIf();


            JSONObject update = new JSONObject();
            update.put(Options.OPT_SHOW_PAUSE_ACTION, true);
            update.put(Options.OPT_SHOW_PLAY_ACTION, false);
            update.put(Options.OPT_SHOW_STOP_ACTION, true);
            Utils.updateNotification(update, notification);

            SoundManager.resumeSound(notification, true);



        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " onReceive :: ", e);
        }
    }



    /**
     * Set the text if any remote input is given.
     *
     * @param action The action where to look for.
     * @param data   The object to extend.
     */
    private void setTextInput(String action, JSONObject data) {
        Bundle input = RemoteInput.getResultsFromIntent(getIntent());

        if (input == null)
            return;

        try {
            data.put("text", input.getCharSequence(action));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launch app if requested by user.
     */
    private void launchAppIf() {
        boolean doLaunch = getIntent().getBooleanExtra(EXTRA_LAUNCH, true);

        if (!doLaunch)
            return;

        launchApp();
    }


}
