package firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.visualphysics.R;
import com.visualphysics.SplashScreenActivity;
import com.zoho.salesiqembed.ZohoSalesIQ;
import java.util.Map;
import Utils.ErrorLog;

/**
 * Created by iziss on 8/5/17.
 */
public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = MessagingService.class.getSimpleName();
    private final String CHANNEL_ID = "VP_Zoho_Channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.

       /* try {
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

            generateNotification(this, remoteMessage.getNotification().getBody());
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/

        Map extras = remoteMessage.getData();
        ZohoSalesIQ.Notification.handle(this.getApplicationContext(),extras, R.mipmap.ic_launcher);

    }


    private void generateNotification(Context context, String message) {
        try {


            int icon = R.mipmap.ic_launcher;
            long when = System.currentTimeMillis();
            Intent notificationIntent = new Intent(context, SplashScreenActivity.class);

            //This is used to navigate flow to splash screen screen
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            Resources res = context.getResources();

            /*This is deprecated*/
           // Notification notification = new NotificationCompat.Builder(context)

            /*Changes after library update*/
            Notification notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_PROMO)
                    .setContentTitle("VisualPhysics")
                    .setContentText(message)
                    .setSmallIcon(getNotificationIcon())
                    .setLargeIcon(BitmapFactory.decodeResource(res, icon))
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_SOUND).build();

            nm.notify(456, notification);
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorLog.SendErrorReport(ex);
        }
    }


    public static int getNotificationIcon() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return R.mipmap.icon_white_notification;
        } else {
            return R.mipmap.ic_launcher;
        }

        /*boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.icon_silhouette : R.drawable.ic_launcher;*/
    }
}


