package org.fawzone.applauncher;


import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import de.appplant.cordova.plugin.notification.Notification;

import static android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

public class AppLauncher {

    public static final String TAG = "AppLauncher";


    public static final String ACTION_FORCE_RELOAD_BACKGROUND = "AppLauncher-forceReloadBackground";

    /**
     * Get activity instance from desired context.
     */
    public static Activity getActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) return (Activity) context;
        if (context instanceof ContextWrapper) return getActivity(((ContextWrapper)context).getBaseContext());
        return null;
    }

    public static void unlockLockScreen(Notification notification){

        if (!notification.getOptions().getAppLauncherEnable()) {
            Log.i(TAG, " ! AppLauncher.enable => do nothing  ");
            return;
        }

        Log.i(TAG, "- unlockLockScreen");
/*
        DevicePolicyManager devicePolicyMngr= (DevicePolicyManager) notification.getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName compName=new ComponentName(notification.getContext(), MyAdmin.class);
        if(!devicePolicyMngr.isAdminActive(compName)) {
            devicePolicyMngr.removeActiveAdmin(compName);
        }
*/

  /*
        //Get the window from the context
        WindowManager wm = (WindowManager)notification.getContext().getSystemService(Context.WINDOW_SERVICE);

//Unlock
        Window window =  getActivity(notification.getContext()).getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
*/
/*
        KeyguardManager km = (KeyguardManager) notification.getContext().getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km .newKeyguardLock("MyKeyguardLock");
        kl.disableKeyguard();

        PowerManager pm = (PowerManager) notification.getContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        wakeLock.acquire();
*/

/*

        KeyguardManager km = (KeyguardManager) notification.getContext().getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km .newKeyguardLock("awqat:"+TAG);
        kl.disableKeyguard();

        PowerManager pm = (PowerManager) notification.getContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "awqat:"+TAG);
        wakeLock.acquire();

*/

        KeyguardManager keyguardManager = (KeyguardManager)notification.getContext().getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Activity.KEYGUARD_SERVICE);
        lock.disableKeyguard();


    }



    public static void forceMainActivityReload(Notification notification) {
        Log.i(TAG, " forceMainActivityReload  ");
        if (!notification.getOptions().getAppLauncherEnable()) {
            Log.i(TAG, " ! AppLauncher.enable => do nothing  ");
            return;
        }

        if (!notification.getOptions().getAppLauncherLaunch()) {
            Log.i(TAG, " ! AppLauncher.launch => do nothing  ");
            return;
        }

        Log.i(TAG, ACTION_FORCE_RELOAD_BACKGROUND+" - Forcing MainActivity reload");
        final PackageManager pm = notification.getContext().getPackageManager();
        final Intent launchIntent = pm.getLaunchIntentForPackage(notification.getContext().getPackageName());
        if (launchIntent == null) {
            Log.w(TAG, ACTION_FORCE_RELOAD_BACKGROUND+" failed to find launchIntent");
            return;
        }

        if(notification.getOptions().getAppLauncherBackground()){
            Log.i(TAG, "  AppLauncher.background => run in background  ");
            launchIntent.setAction(ACTION_FORCE_RELOAD_BACKGROUND);
        } else {
            Log.i(TAG, "  ! AppLauncher.background => run in foreground  ");
        }

        launchIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);//4->FLAG_FROM_BACKGROUND
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);//262144->FLAG_ACTIVITY_NO_USER_ACTION
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);//65536->FLAG_ACTIVITY_NO_ANIMATION

        notification.getContext().startActivity(launchIntent);

    }


    /**
     * Moves the app to the foreground.
     */
    public static void  moveToForeground(Notification notification, Activity app)
    {
        Log.i(TAG, " moveToForeground  ");

        if (!notification.getOptions().getAppLauncherEnable()) {
            Log.i(TAG, " ! AppLauncher.enable => do nothing  ");
            return;
        }

        if (!notification.getOptions().getAppLauncherLaunch()) {
            Log.i(TAG, " ! AppLauncher.launch => do nothing  ");
            return;
        }

       // Context app    = getApp().getApplicationContext();
        String pkgName = app.getPackageName();
        Intent intent =  app.getPackageManager().getLaunchIntentForPackage(pkgName);

        //Intent intent = getLaunchIntent();

        intent.addFlags(
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);

        clearScreenAndKeyguardFlags(app);
        app.startActivity(intent);
    }

    /**
     * Clears required flags to the window to unlock/wakeup the device.
     */
    private static void clearScreenAndKeyguardFlags(Activity app)
    {
        app.runOnUiThread(() -> app.getWindow().clearFlags(FLAG_ALLOW_LOCK_WHILE_SCREEN_ON | FLAG_SHOW_WHEN_LOCKED | FLAG_TURN_SCREEN_ON | FLAG_DISMISS_KEYGUARD));
    }

}



