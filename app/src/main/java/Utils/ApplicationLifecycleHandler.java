package Utils;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.visualphysics.MyApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by India on 7/12/2016.
 */
public class ApplicationLifecycleHandler implements MyApplication.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private static final String TAG = ApplicationLifecycleHandler.class.getSimpleName();
    private static boolean isInBackground = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        Log.i(TAG, "App Created.....................");
        checkSyncAndLocalDate(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.i(TAG, "App Started.....................");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.i(TAG, "App Resumed.....................");
        if (isInBackground) {
            //Log.d(TAG, "app went to foreground");
            isInBackground = false;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.i(TAG, "App Pause.....................");
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.i(TAG, "App Destroyed.....................");
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onTrimMemory(int i) {
        if (i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            isInBackground = true;
        }
    }

    private void checkSyncAndLocalDate(Activity activity) {
        try {

            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            Calendar calendar = Calendar.getInstance(timeZone);
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            simpleDateFormat.setTimeZone(timeZone);

            System.out.println("Time zone: " + timeZone.getID());
            System.out.println("default time zone: " + TimeZone.getDefault().getID());
            System.out.println();

            System.out.println("UTC:     " + simpleDateFormat.format(calendar.getTime()));
            System.out.println("Default: " + calendar.getTime());

            AppUtil mAppUtils = new AppUtil(activity.getApplicationContext());
            SharedPrefrences mSharedPref = new SharedPrefrences(activity.getApplicationContext());
            if (!mSharedPref.getPreferences(mSharedPref.LAST_APP_OPEN_DATE, "").equals("")) {

                SimpleDateFormat UTCTimeDateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a");
                Date LocalUTCDateTime = UTCTimeDateFormat.parse(mAppUtils.getUTCTime());
                String dd1 = UTCTimeDateFormat.format(LocalUTCDateTime);
                LocalUTCDateTime = UTCTimeDateFormat.parse(dd1);

                String dd = mSharedPref.getPreferences(mSharedPref.LAST_APP_OPEN_DATE, "");
                Date SyncDateTime = UTCTimeDateFormat.parse(mSharedPref.getPreferences(mSharedPref.LAST_APP_OPEN_DATE, ""));

               if(LocalUTCDateTime.before(SyncDateTime)){
                   //Toast.makeText(activity.getApplicationContext(),"Date Time has been change please correct the date.",Toast.LENGTH_LONG).show();
                   //activity.finish();
               }

            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
    }
}
