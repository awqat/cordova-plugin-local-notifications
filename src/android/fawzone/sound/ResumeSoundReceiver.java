package org.fawzone.sound;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.receiver.AbstractClickReceiver;
import de.appplant.cordova.plugin.notification.util.AssetUtil;

import static de.appplant.cordova.plugin.localnotification.LocalNotification.fireEvent;
import static de.appplant.cordova.plugin.notification.Options.EXTRA_LAUNCH;

/**
 * The receiver activity is triggered when a notification is clicked by a user.
 * The activity calls the background callback and brings the launch intent
 * up to foreground.
 */
public class ResumeSoundReceiver extends AbstractClickReceiver {


    /**
     * Called when local notification was clicked by the user.
     *
     * @param notification Wrapper around the local notification.
     * @param bundle       The bundled extras.
     */
    @Override
    public void onClick(Notification notification, Bundle bundle) {
        String action   = getAction();
        JSONObject data = new JSONObject();
        Log.e("ResumeSoundReceiver", " > onClick " +action);
        setTextInput(action, data);
        launchAppIf();

        /*
        AssetUtil assets = AssetUtil.getInstance(this);

        // create a handler to post messages to the main thread
        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(notification.getContext(), "PLAY", Toast.LENGTH_SHORT); // For example

                // toast.setMargin(10,10);
                toast.setGravity(Gravity.BOTTOM, 10, 10);
                LinearLayout toastContentView = (LinearLayout) toast.getView();
                ImageView imageView = new ImageView(notification.getContext());
                //imageView.setImageBitmap(options.getRingerModeVibrateIconNormal());

                imageView.setImageURI(assets.parse("res://ic_action_play.png"));
                //imageView.setBackgroundColor(Color.GRAY);

                toastContentView.addView(imageView, 0);

                toast.show();
            }
        });
*/
        SoundManager.resumeSound(notification, true);
        


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
