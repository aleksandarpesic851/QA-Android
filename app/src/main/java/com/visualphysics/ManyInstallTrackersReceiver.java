package com.visualphysics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mixpanel.android.mpmetrics.InstallReferrerReceiver;

/**
 * To get the reffer code on the install from the google play
 */
public class ManyInstallTrackersReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {

            //For Mixpanel Referrer
            InstallReferrerReceiver mixpanelReferrerTracking = new InstallReferrerReceiver();
            mixpanelReferrerTracking.onReceive(context, intent);

            //Old One generally used for Google Referrer
            if (intent.hasExtra("referrer")) {
                String referrer = intent.getStringExtra("referrer");
            }

        }
    }


}
