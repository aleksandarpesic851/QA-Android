package mixpanel;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.mixpanel.android.mpmetrics.MPConfig;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.mpmetrics.ResourceIds;
import com.mixpanel.android.util.MPLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by iziss on 10/4/18.
 */

/**
 * BroadcastReceiver for handling Google Cloud Messaging intents.
 * <p/>
 * <p>You can use GCMReceiver to report Google Cloud Messaging registration identifiers
 * to Mixpanel, and to display incoming notifications from Mixpanel to
 * the device status bar. Together with {@link MixpanelAPI.People#initPushHandling(String) }
 * this is the simplest way to get up and running with notifications from Mixpanel.
 * <p/>
 * <p>To enable GCMReceiver in your application, add a clause like the following
 * to the &lt;application&gt; tag of your AndroidManifest.xml. (Be sure to replace "YOUR APPLICATION PACKAGE NAME"
 * in the snippet with the actual package name of your app.)
 * <p/>
 * <pre>
 * {@code
 *
 * <receiver android:name="com.mixpanel.android.mpmetrics.GCMReceiver"
 * android:permission="com.google.android.c2dm.permission.SEND" >
 * <intent-filter>
 * <action android:name="com.google.android.c2dm.intent.RECEIVE" />
 * <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
 * <category android:name="YOUR APPLICATION PACKAGE NAME" />
 * </intent-filter>
 * </receiver>
 *
 * }
 * </pre>
 * <p/>
 * <p>In addition, GCMReceiver will also need the following permissions configured
 * in your AndroidManifest.xml file:
 * <p/>
 * <pre>
 * {@code
 *
 * <!-- Be sure to change YOUR_PACKAGE_NAME to the real name of your application package -->
 * <permission android:name="YOUR_PACKAGE_NAME.permission.C2D_MESSAGE" android:protectionLevel="signature" />
 * <uses-permission android:name="YOUR_PACKAGE_NAME.permission.C2D_MESSAGE" />
 *
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.GET_ACCOUNTS" />
 * <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
 * <uses-permission android:name="android.permission.WAKE_LOCK" />
 *
 * }
 * </pre>
 * <p/>
 * <p>Once the GCMReceiver is configured, the only thing you have to do to
 * get set up Mixpanel messages is call {@link MixpanelAPI.People#identify(String) }
 * with a distinct id for your user, and call {@link MixpanelAPI.People#initPushHandling(String) }
 * with the your Google API project identifier.
 * <pre>
 * {@code
 *
 * MixpanelAPI.People people = mMixpanelAPI.getPeople();
 * people.identify("A USER DISTINCT ID");
 * people.initPushHandling("123456789123");
 *
 * }
 * </pre>
 * <p/>
 * <p>If you would prefer to handle either sending a registration id to Mixpanel yourself
 * but allow GCMReceiver to handle displaying Mixpanel messages, remove the
 * REGISTRATION intent from the GCMReceiver {@code <receiver> } tag, and call
 * {@link MixpanelAPI.People#setPushRegistrationId(String)}
 * in your own REGISTRATION handler.
 *
 * @see MixpanelAPI#getPeople()
 * @see MixpanelAPI.People#initPushHandling(String)
 * @see <a href="https://mixpanel.com/docs/people-analytics/android-push">Getting Started with Android Push Notifications</a>
 */
public class MixpanelCustomGCMReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if ("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
            handleRegistrationIntent(context, intent);
        } else if ("com.google.android.c2dm.intent.RECEIVE".equals(action)) {
            handleNotificationIntent(context, intent);
        }

    }

    protected static class NotificationData {

        private NotificationData(int anIcon, int aLargeIcon, int aWhiteIcon, CharSequence aTitle, String aMessage, final String aImageURL, Intent anIntent, int aColor) {

            icon = anIcon;
            largeIcon = aLargeIcon;
            whiteIcon = aWhiteIcon;
            title = aTitle;
            message = aMessage;
            intent = anIntent;
            color = aColor;
            imageURL = aImageURL;


        }

        public final int icon;
        public final int largeIcon;
        public final int whiteIcon;
        public final CharSequence title;
        public final String message;
        public final Intent intent;
        public final int color;
        public String imageURL;
        public Bitmap bitMapImage;

        public static final int NOT_SET = -1;
    }

    /**
     * Get default Intent
     *
     * @param context
     * @return
     */
    Intent getDefaultIntent(Context context) {
        final PackageManager manager = context.getPackageManager();
        return manager.getLaunchIntentForPackage(context.getPackageName());
    }


    /***
     * This will read value from key from Intent and return them in a Object of NotificationData
     *
     * @param context
     * @param inboundIntent
     * @param iconIds
     * @return
     */
    NotificationData readInboundIntent(Context context, Intent inboundIntent, ResourceIds iconIds) {

        final PackageManager manager = context.getPackageManager();

        final String message = inboundIntent.getStringExtra("mp_message");
        final String iconName = inboundIntent.getStringExtra("mp_icnm");
        final String largeIconName = inboundIntent.getStringExtra("mp_icnm_l");
        final String whiteIconName = inboundIntent.getStringExtra("mp_icnm_w");
        final String uriString = inboundIntent.getStringExtra("mp_cta");
        CharSequence notificationTitle = inboundIntent.getStringExtra("mp_title");
        final String colorName = inboundIntent.getStringExtra("mp_color");
        final String campaignId = inboundIntent.getStringExtra("mp_campaign_id");
        final String messageId = inboundIntent.getStringExtra("mp_message_id");
        final String extraLogData = inboundIntent.getStringExtra("mp");

        //Image url will be handled in this parameter
        final String imageURL = inboundIntent.getStringExtra("mp_image");

        int color = NotificationData.NOT_SET;

        //Track campaign if campaignId received
        trackCampaignReceived(context, campaignId, messageId, extraLogData);

        if (colorName != null) {
            try {
                color = Color.parseColor(colorName);
            } catch (IllegalArgumentException e) {
            }
        }

        if (message == null) {
            return null;
        }

        int notificationIcon = -1;
        if (null != iconName) {
            if (iconIds.knownIdName(iconName)) {
                notificationIcon = iconIds.idFromName(iconName);
            }
        }

        int largeNotificationIcon = NotificationData.NOT_SET;
        if (null != largeIconName) {
            if (iconIds.knownIdName(largeIconName)) {
                largeNotificationIcon = iconIds.idFromName(largeIconName);
            }
        }

        int whiteNotificationIcon = NotificationData.NOT_SET;
        if (null != whiteIconName) {
            if (iconIds.knownIdName(whiteIconName)) {
                whiteNotificationIcon = iconIds.idFromName(whiteIconName);
            }
        }

        ApplicationInfo appInfo;
        try {
            appInfo = manager.getApplicationInfo(context.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            appInfo = null;
        }

        if (notificationIcon == NotificationData.NOT_SET && null != appInfo) {
            notificationIcon = appInfo.icon;
        }

        if (notificationIcon == NotificationData.NOT_SET) {
            notificationIcon = android.R.drawable.sym_def_app_icon;
        }

        if (null == notificationTitle && null != appInfo) {
            notificationTitle = manager.getApplicationLabel(appInfo);
        }

        if (null == notificationTitle) {
            notificationTitle = "A message for you";
        }

        final Intent notificationIntent = buildNotificationIntent(context, uriString, campaignId, messageId, extraLogData);


        return new NotificationData(notificationIcon, largeNotificationIcon, whiteNotificationIcon, notificationTitle, message, imageURL, notificationIntent, color);
    }

    /***
     * Simply generate Intent for notification
     *
     * @param context
     * @param uriString
     * @param campaignId
     * @param messageId
     * @param extraLogData
     * @return
     */
    private Intent buildNotificationIntent(Context context, String uriString, String campaignId, String messageId, String extraLogData) {
        Uri uri = null;
        if (null != uriString) {
            uri = Uri.parse(uriString);
        }

        final Intent ret;
        if (null == uri) {
            ret = getDefaultIntent(context);
        } else {
            ret = new Intent(Intent.ACTION_VIEW, uri);
        }

        if (campaignId != null) {
            ret.putExtra("mp_campaign_id", campaignId);
        }

        if (messageId != null) {
            ret.putExtra("mp_message_id", messageId);
        }

        if (extraLogData != null) {
            ret.putExtra("mp", extraLogData);
        }

        return ret;
    }

    private void buildNotification(Context context, Intent inboundIntent, ResourceIds iconIds) {

        final NotificationData notificationData = readInboundIntent(context, inboundIntent, iconIds);

        //Background method to download image and show in Push Notification
        new GeneratePictureStyleNotification(context, notificationData).execute();

       /* if (null == notificationData) {
            return null;
        }

        MPLog.d(LOGTAG, "MP GCM notification received: " + notificationData.message);
        final PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                notificationData.intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        final Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = makeNotificationSDK26OrHigher(context, contentIntent, notificationData);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = makeNotificationSDK21OrHigher(context, contentIntent, notificationData);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = makeNotificationSDK16OrHigher(context, contentIntent, notificationData);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            notification = makeNotificationSDK11OrHigher(context, contentIntent, notificationData);
        } else {
            notification = makeNotificationSDKLessThan11(context, contentIntent, notificationData);
        }

        return notification;*/
    }

    private void handleRegistrationIntent(Context context, Intent intent) {

        final String registration = intent.getStringExtra("registration_id");
        if (intent.getStringExtra("error") != null) {
            MPLog.e(LOGTAG, "Error when registering for GCM: " + intent.getStringExtra("error"));
        } else if (registration != null) {
            MPLog.d(LOGTAG, "Registering GCM ID: " + registration);

            //Added by IZISS to add the ID
            new MixPanelClass(context).getInstance(context).getPeople().setPushRegistrationId(registration);

        } else if (intent.getStringExtra("unregistered") != null) {
            MPLog.d(LOGTAG, "Unregistering from GCM");

            //Added by IZISS to remove the ID
            new MixPanelClass(context).getInstance(context).getPeople().clearPushRegistrationId();
        }
    }

    /**
     * This will handle notification
     *
     * @param context
     * @param intent
     */
    private void handleNotificationIntent(Context context, Intent intent) {

        final MPConfig config = MPConfig.getInstance(context);
        String resourcePackage = config.getResourcePackageName();
        if (null == resourcePackage) {
            resourcePackage = context.getPackageName();
        }

//        final ResourceIds drawableIds = new ResourceReader.Drawables(resourcePackage, context);

        //Update by IZISS to get drawable from custom value
        final ResourceIds drawableIds = new CustomResourceReader.Drawables(resourcePackage, context);
        final Context applicationContext = context.getApplicationContext();

        //IZISS change flow from here
        buildNotification(applicationContext, intent, drawableIds);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(9)
    protected Notification makeNotificationSDKLessThan11(Context context, PendingIntent intent, NotificationData notificationData) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context).
                setSmallIcon(notificationData.icon).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setDefaults(MPConfig.getInstance(context).getNotificationDefaults());

        if (notificationData.largeIcon != NotificationData.NOT_SET) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationData.largeIcon));
        }

        final Notification n = builder.getNotification();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(11)
    protected Notification makeNotificationSDK11OrHigher(Context context, PendingIntent intent, NotificationData notificationData) {
        final Notification.Builder builder = new Notification.Builder(context).
                setSmallIcon(notificationData.icon).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setDefaults(MPConfig.getInstance(context).getNotificationDefaults());

        if (notificationData.largeIcon != NotificationData.NOT_SET) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationData.largeIcon));
        }

        final Notification n = builder.getNotification();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    @SuppressLint("NewApi")
    @TargetApi(16)
    protected Notification makeNotificationSDK16OrHigher(Context context, PendingIntent intent, NotificationData notificationData) {
        final Notification.Builder builder = new Notification.Builder(context).
                setSmallIcon(notificationData.icon).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setStyle(getNotificationStyle(notificationData)).
                setDefaults(MPConfig.getInstance(context).getNotificationDefaults());

        if (notificationData.largeIcon != NotificationData.NOT_SET) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationData.largeIcon));
        }

        final Notification n = builder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    @SuppressLint("NewApi")
    @TargetApi(21)
    protected Notification makeNotificationSDK21OrHigher(Context context, PendingIntent intent, NotificationData notificationData) {
        final Notification.Builder builder = new Notification.Builder(context).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setStyle(getNotificationStyle(notificationData)).
                setDefaults(MPConfig.getInstance(context).getNotificationDefaults());

        if (notificationData.whiteIcon != NotificationData.NOT_SET) {
            builder.setSmallIcon(notificationData.whiteIcon);
        } else {
            builder.setSmallIcon(notificationData.icon);
        }

        if (notificationData.largeIcon != NotificationData.NOT_SET) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationData.largeIcon));
        }

        if (notificationData.color != NotificationData.NOT_SET) {
            builder.setColor(notificationData.color);
        }

        final Notification n = builder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    @SuppressLint("NewApi")
    @TargetApi(26)
    protected Notification makeNotificationSDK26OrHigher(Context context, PendingIntent intent, NotificationData notificationData) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = MPConfig.getInstance(context).getNotificationChannelId();
        String channelName = MPConfig.getInstance(context).getNotificationChannelName();
        int importance = MPConfig.getInstance(context).getNotificationChannelImportance();

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        int notificationDefaults = MPConfig.getInstance(context).getNotificationDefaults();
        if (notificationDefaults == Notification.DEFAULT_VIBRATE || notificationDefaults == Notification.DEFAULT_ALL) {
            channel.enableVibration(true);
        }
        if (notificationDefaults == Notification.DEFAULT_LIGHTS || notificationDefaults == Notification.DEFAULT_ALL) {
            channel.enableLights(true);
            channel.setLightColor(Color.WHITE);
        }
        mNotificationManager.createNotificationChannel(channel);

        final Notification.Builder builder = new Notification.Builder(context).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setStyle(getNotificationStyle(notificationData)).
                setDefaults(MPConfig.getInstance(context).getNotificationDefaults()).
                setChannelId(channelId);

        if (notificationData.whiteIcon != NotificationData.NOT_SET) {
            builder.setSmallIcon(notificationData.whiteIcon);
        } else {
            builder.setSmallIcon(notificationData.icon);
        }

        if (notificationData.largeIcon != NotificationData.NOT_SET) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationData.largeIcon));
        }

        if (notificationData.color != NotificationData.NOT_SET) {
            builder.setColor(notificationData.color);
        }

        final Notification n = builder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    private void trackCampaignReceived(Context context, final String campaignId, final String messageId, final String extraLogData) {
        if (campaignId != null && messageId != null) {

            //Added by IZISS to remove the ID
            MixpanelAPI api = new MixPanelClass(context).getInstance(context);

            {
                if (api.isAppInForeground()) {
                    JSONObject pushProps = new JSONObject();
                    try {
                        if (extraLogData != null) {
                            pushProps = new JSONObject(extraLogData);
                        }
                    } catch (JSONException e) {
                    }

                    try {
                        pushProps.put("campaign_id", Integer.valueOf(campaignId).intValue());
                        pushProps.put("message_id", Integer.valueOf(messageId).intValue());
                        pushProps.put("message_type", "push");
                        api.track("$campaign_received", pushProps);
                    } catch (JSONException e) {
                    }
                }
            }
        }
    }


    @SuppressWarnings("unused")
    private static final String LOGTAG = "MixpanelAPI.GCMReceiver";

    /***
     * This will return style after checking is it for Picture or only text
     *
     * @param notificationData
     * @return
     */
    private Notification.Style getNotificationStyle(NotificationData notificationData) {

        if (notificationData.bitMapImage != null) {

            return new Notification.BigPictureStyle().bigPicture(notificationData.bitMapImage).
                    setSummaryText(notificationData.message).
                    setBigContentTitle(notificationData.title);
        } else {
            return new Notification.BigTextStyle().bigText(notificationData.message);
        }

    }

    /**
     * This will generate image from URL and show it in notification
     * https://stackoverflow.com/questions/24840282/load-image-from-url-in-notification-android
     */
    public class GeneratePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context context;
        private NotificationData notificationData;

        public GeneratePictureStyleNotification(Context context, NotificationData notificationData) {
            super();
            this.context = context;
            this.notificationData = notificationData;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;

            try {
                URL url = new URL(notificationData.imageURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            try {

                this.notificationData.bitMapImage = result;

                if (null != notificationData) {

                    MPLog.d(LOGTAG, "MP GCM notification received: " + notificationData.message);

                    final PendingIntent contentIntent = PendingIntent.getActivity(
                            context,
                            0,
                            notificationData.intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

                    final Notification notification;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notification = makeNotificationSDK26OrHigher(context, contentIntent, notificationData);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        notification = makeNotificationSDK21OrHigher(context, contentIntent, notificationData);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        notification = makeNotificationSDK16OrHigher(context, contentIntent, notificationData);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        notification = makeNotificationSDK11OrHigher(context, contentIntent, notificationData);
                    } else {
                        notification = makeNotificationSDKLessThan11(context, contentIntent, notificationData);
                    }

                    if (null != notification) {
                        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, notification);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
