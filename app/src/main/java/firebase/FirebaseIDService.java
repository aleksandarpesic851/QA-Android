package firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

import zoho.ZohoUtils;

/**
 * Created by iziss on 8/5/17.
 * Reference: https://stackoverflow.com/questions/51123197/firebaseinstanceidservice-is-deprecated
 */
/*Library Update FirebaseInstanceService is deprecated and we have to use FirebaseMessagingService*/
public class FirebaseIDService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseIDService";


    /*Deprecated code*/
    /*@Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.

        sendRegistrationToServer(refreshedToken);
    }*/

    /*Code after library update*/
    @Override
    public void onNewToken(String newToken) {
        super.onNewToken(newToken);
        Log.d("NEW_TOKEN", newToken);
        sendRegistrationToServer(newToken);

    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.

        //This will send firebase device token to ZOHO
        ZohoUtils.setPushToken();

        Log.i("Token>>", token);

    }
}
