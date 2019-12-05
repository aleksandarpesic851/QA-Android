package firebase;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.visualphysics.MyApplication;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.SharedPrefrences;

/**
 * Created by iziss on 8/5/17.
 */
public class FirebaseClass {

    private FirebaseAnalytics firebaseAnalytics;


    public FirebaseClass(Context context) {
        // Obtain the Firebase Analytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void sendAnalyticsData(int itemID, String itemName, String eventType) {

        try {
            Bundle bundle = new Bundle();
            bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, itemID);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);

            //Logs an app event.
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            //Sets whether analytics collection is enabled for this app on this device.
            firebaseAnalytics.setAnalyticsCollectionEnabled(true);

            //Sets the minimum engagement time required before starting a session. The default value is 10000 (10 seconds). Let's make it 20 seconds just for the fun
            firebaseAnalytics.setMinimumSessionDuration(20000);

            //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes).
            firebaseAnalytics.setSessionTimeoutDuration(500);

            //Sets the user ID property.
            firebaseAnalytics.setUserId(String.valueOf(itemID));

            //Sets a user property to a given value.
            firebaseAnalytics.setUserProperty(eventType, itemName);

            Log.i("Analytics Data>", "ID: " + itemID + " Item Name: " + itemName + " Type: " + eventType);
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorLog.SendErrorReport(ex);
        }

    }

    public void sendCustomAnalyticsData(String event, String eventName) {

        try {
            Bundle bundle = new Bundle();
            bundle.putString("User_Id", getUserID());
            bundle.putString("Event_Name", eventName);

            //Sets whether analytics collection is enabled for this app on this device.
            firebaseAnalytics.setAnalyticsCollectionEnabled(true);

            //Sets the minimum engagement time required before starting a session. The default value is 10000 (10 seconds). Let's make it 20 seconds just for the fun
            firebaseAnalytics.setMinimumSessionDuration(20000);

            //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes).
            firebaseAnalytics.setSessionTimeoutDuration(500);

            //Sets the user ID property.
            firebaseAnalytics.setUserId(String.valueOf(getUserID()));

            //Logs an app event.
            firebaseAnalytics.logEvent(event, bundle);

            Log.i("Analytics Data>", "User ID: " + getUserID() + " Event: " + event + " Event Name: " + eventName);

        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorLog.SendErrorReport(ex);
        }

       /* bundle.putString(FirebaseAnalytics.Event.SIGN_UP,itemName);
        bundle.putString(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE,itemName);
        bundle.putString(FirebaseAnalytics.Event.APP_OPEN,itemName);*/

    }

    private String getUserID() {

        SharedPrefrences mSharedPrefrences = new SharedPrefrences(MyApplication.getContext());
        LoginUser mLoginUser = mSharedPrefrences.getLoginUser();

        if (mLoginUser != null) {
            if (mLoginUser.getStudentID() != null) {
                return mLoginUser.getStudentID();
            }
        }
        return "";
    }

}
