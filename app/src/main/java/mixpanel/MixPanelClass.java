package mixpanel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.visualphysics.ApiCall;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.NotificationsUtils;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import fragment.model.Questionnaire;

/**
 * Created by iziss on 17/10/17.
 */
public class MixPanelClass implements OnTaskCompleted.CallBackListener {

    // Preference Variable
    public static final String IS_FIRST_CHAPTER = "isFirstChapter"; //1
    public static final String IS_FIRST_VIDEO = "isFirstVideo"; //3
    public static final String IS_VIDEO_SEEN_AFTER_UPDATE = "isUpdatedVideoSeen"; //3
    public static final String FIRST_NAME_FROM_SOCIAL_LOGIN = "firstName"; //4
    public static final String IS_FROM_FREE_TRIAL = "isFromFreeTrial"; //5
    public static final String IS_EMAIL_CHECKED_ON_MIXPANEL = "isEmailChecked"; //6

    //MPE --> Mix Panel Event
    public static final String MPE_REFERRAL_CODE_SCREEN = "Referral_Code_Screen";
    public static final String MPE_FIRST_CHAPTER_CLICKED = "First_Chapter_Click"; //1
    public static final String MPE_CHAPTER_CLICKED = "Chapter_Click"; //2
    public static final String MPE_FIRST_VIDEO_SEEN = "First_Video_Seen"; //3
    public static final String MPE_VIDEO_SEEN = "Video_Seen"; //4

    public static final String MPE_BOUGHT = "Bought_vp";//5
    public static final String MPE_PAYMENT_ABANDONED = "Payment_Abandoned"; //6
    public static final String MPE_PAYMENT_FAILED = "Payment_Failed"; //6
    public static final String MPE_COUPON_USED = "Coupon_Used"; //7
    public static final String MPE_DOWNLOAD_VIDEO = "Download_Video"; //8_1
    public static final String MPE_DOWNLOAD_CHAPTER = "Download_Chapter"; //8_2

    //Added on 29 Nov 2018
    public static final String MPE_LIKE_VIDEO_THUMBS_UP = "Thumbs_Up"; //8_3

    public static final String MPE_LOGIN = "Login";
    public static final String MPE_APP_OPEN = "AppOpen";
    public static final String MPE_QUESTIONNAIRE_SUBMIT = "Questionnaire_Submit";

    public static final String MPE_NO_REFERRAL = "No_Referral";

    public static final String MPE_INSTALL_WIHT_NO_REFERRAL_CODE = "Install_With_No_Referral_Code";
    public static final String MPE_SUBSCRIPTION_EXPIRED = "Subscription_Expired";

    //MPE --> Mix Panel People Attribute
    public static final String MPA_EMAIL = "$email";
    public static final String MPA_NAME = "$name";
    public static final String MPA_REGISTER_DATE = "Register_Date";
    public static final String MPA_PACKAGE = "Package";
    public static final String MPA_SUBSCRIPTION_END_DATE = "Subscription_End_Date";
    public static final String MPA_REFERRAL_CODE = "Referral_Code";

    public static final String MPA_FRIEND_REFERRAL_CODE = "Friend_Referral_Code";
    public static final String MPA_VIDEO_PLAY_COUNT = "Video_Play_Count";
    public static final String MPA_LAST_SESSION = "Last_Session";
    public static final String MPA_PAYMENT_ABANDONED = "Payment_Abandoned";

    public static final String MPA_TOTAL_WATCH_DURATION = "Total_Watch_Duration";

    public static final String MPA_USE = "vp_use";
    public static final String MPA_EXAM = "vp_exam";
    public static final String MPA_YEAR = "vp_year";
    public static final String MPA_LAST_CHAPTER_CLICKED = "Last_Chapter_Clicked";

    public static final String MPA_IS_NOTIFICATION_ENABLED = "Is_Notification_Enabled";

    public static boolean IS_REPEAT_LOGIN_WITH_DIFFERENT_EMAIL = false;

    /*Added to manage guides*/
    public static String PREF_IS_FIRST_LOGIN = "isFirstLogin";
    public static String PREF_IS_CHAPTER_GUIDE_DONE = "isChapterGuideDone";
    public static String PREF_IS_VIDEO_LIST_GUIDE_DONE = "isVideoListGuideDone";
    public static String PREF_IS_VIDEO_GUIDE_DONE = "isVideoGuideDone";

    public static final String PREF_IS_QUESTIONNAIRE_COMPLETED = "isQuestionnaireCompleted";

    //Preference name and other references
    public static final String MIX_PANEL_PREFERENCE = "MixPanel_VisualPhysics_Preference";
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Context mContext;
    private MixpanelAPI mixPanel;

    //Default TagID
    public static final String DEFAULT_TAG_ID = "5";

    //Mix Panel key for Live
    //public static final String MIX_PANEL_TOKEN = "3863a24c31341d2b8e8658b90c957ce0";

    //Mix Panel key for Testing
    public static final String MIX_PANEL_TOKEN = "a18c3f47228438225b987b5b7e167903";

    //Google Sender Id For Push Messages->> 12 digits can be obtained from Existing Firebase
    private static final String GOOGLE_SENDER_ID = "895611495323";


    //For Email check at Mixpanel
    private ApiCall mApiCall;
    private AppUtil mAppUtils;


    /***
     * Initializing Mixpanel
     *
     * @param mContext
     */
    public MixPanelClass(Context mContext) {

        this.mContext = mContext;

        if (mContext != null) {

            //Initialize shared preference
            sharedPref = mContext.getSharedPreferences(MIX_PANEL_PREFERENCE, Context.MODE_PRIVATE);

            //get editor to put values
            editor = sharedPref.edit();

            mApiCall = new ApiCall();
            mAppUtils = new AppUtil(mContext);

            // Initialize the library with your
            // Mixpanel project token, MIXPANEL_TOKEN, and a reference
            // to your application context..
            mixPanel = MixpanelAPI.getInstance(mContext, MIX_PANEL_TOKEN);
        }

    }

    /***
     * Initializing Mixpanel
     *
     * @param mContext
     */
    public MixpanelAPI getInstance(Context mContext) {

        // Initialize the library with your
        // Mixpanel project token, MIXPANEL_TOKEN, and a reference
        // to your application context..
        mixPanel = MixpanelAPI.getInstance(mContext, MIX_PANEL_TOKEN);

        return mixPanel;

    }

    /***
     * Will return current Mixpanel
     *
     * @param
     */
    public MixpanelAPI getCurrentInstance() {

        return mixPanel;

    }

    /***
     * Enable/Initialize push notification from MixPanel; Called from sendMixPanelPeopleAttribute()
     */
    public void initializeMixpanelPush() {

        MixpanelAPI.People people = mixPanel.getPeople();
        people.identify(getMixPanelAliasValue(true));

        /*Set Firebase token manually on mixpanel*/
        //Added for firebase push notifications on 18th June 2019
        if (FirebaseInstanceId.getInstance() != null) {
            String firebaseDeviceToken = FirebaseInstanceId.getInstance().getToken();

            if (firebaseDeviceToken != null) {
                people.setPushRegistrationId(firebaseDeviceToken);
            }
        }

        // Commented as this class is deprecated on 18th June 2019
        //people.initPushHandling(GOOGLE_SENDER_ID);




    }

    /***
     * This will create Json from a hash map which will be send on Mixpanel to track
     *
     * @param trackingKey,hashMap
     */
    public void sendData(String trackingKey, HashMap<String, String> hashMap) {

        try {

            JSONObject objectToSend = new JSONObject();

            //With no property values
            if (hashMap != null) {

                //Get the set
                Set set = (Set) hashMap.entrySet();

                //Create iterator on Set
                Iterator iterator = set.iterator();

                while (iterator.hasNext()) {
                    Map.Entry mapEntry = (Map.Entry) iterator.next();

                    // Get Key
                    String keyValue = (String) mapEntry.getKey();

                    //Get Value
                    String value = (String) mapEntry.getValue();

                    try {

                        objectToSend.put(keyValue, value);

                    } catch (Exception ex) {

                        ex.printStackTrace();

                    }
                }

                //Call MixPanel function to send data with properties
                sendToMixPanelData(trackingKey, objectToSend);

            } else {

                //Call MixPanel function to send data without properties
                sendToMixPanelData(trackingKey, objectToSend);
            }

            //Send analytics data to FB
            if (isSendOnFacebook(trackingKey))
                sendFacebookEvent(trackingKey, hashMap);


        } catch (Exception ex) {
            ErrorLog.SendErrorReportMixPanel(ex);
            ex.printStackTrace();
        }
    }

    /***
     * This will send data to MixPanel
     *
     * @param objectToSend
     */
    public void sendToMixPanelData(String trackingKey, JSONObject objectToSend) {

        try {

            //Except this events; this events are performed before video play therefor we need to avoid calling identify
            if (trackingKey.equalsIgnoreCase(MPE_APP_OPEN) || trackingKey.equalsIgnoreCase(MPE_REFERRAL_CODE_SCREEN) || trackingKey.equalsIgnoreCase(MPE_FIRST_CHAPTER_CLICKED) || trackingKey.equalsIgnoreCase(MPE_FIRST_VIDEO_SEEN) || trackingKey.equalsIgnoreCase(MPE_QUESTIONNAIRE_SUBMIT)) {

            } else {

                //This method will identify Mixpanel User
                // Ensure all future events sent from
                // the device will have the distinct_id
                identifyMixpanelUser();
            }


            //This function is commented because it will create new profile on Mixpanel
            //identifyPeopleUser(true);

            mixPanel.track(trackingKey, objectToSend);

        } catch (Exception ex) {
            Log.e("Visual Physics", "Unable to add properties to JSONObject through Mix Panel", ex);
            ErrorLog.SendErrorReportMixPanel(ex);
        }
    }

    /***
     * This is called after first video seen and should only be called once for each device
     *
     * @param
     */

    public void setUserIdentity(boolean isNewUserWithNewDevice) {

        try {

            if (mixPanel != null) {

                mixPanel.alias((getMixPanelAliasValue(false)), null);

                //Set People attribute
                sendMixPanelPeopleAttribute(true, isNewUserWithNewDevice, false);
            }
        } catch (Exception ex) {
            ErrorLog.SendErrorReportMixPanel(ex);
            ex.printStackTrace();
        }
    }

    /***
     * Identify Mixpanel User
     */
    public void identifyMixpanelUser() {

        if (mixPanel != null) {
            mixPanel.identify(getMixPanelAliasValue(false));
        }
    }

    /***
     * Identify Mixpanel People User
     */
    public void identifyPeopleUser(boolean value) {

        mixPanel.getPeople().identify(getMixPanelAliasValue(value));

    }


    /***
     * This will send the People attribute to mixpanel just after first video played
     *
     * @param
     */

    public void sendMixPanelPeopleAttribute(boolean isAfterFirstVideo, boolean isNewUserWithNewDevice, boolean isSameUserLoginAgain) {
        try {

            if (mixPanel != null) {

                LoginUser mUser = new SharedPrefrences(mContext).getLoginUser();

                if (mUser != null) {

                    identifyPeopleUser(true);

                    //If a fresh user
                    if ((isNewUserWithNewDevice || isSameUserLoginAgain) || isAfterFirstVideo) {

                        mixPanel.getPeople().set(MPA_EMAIL, mUser.getEmail());
                    }

                        /*Implemented to restrict to send email and register date for condition when a different user is login in the same device*/
                        if (isNewUserWithNewDevice) {

                            //To Send Register only once when a user is login as a fresh user
                        mixPanel.getPeople().set(MPA_REGISTER_DATE, mUser.getCreatedDate());

                    }

                    if (!isSameUserLoginAgain) {

                        mixPanel.getPeople().set(MPA_PACKAGE, "Free");

                    } else {

                        //Only login event for repeated login
                        sendData(MPE_LOGIN, null);
                    }

                    //Default attributes updated with every people attribute time
                    mixPanel.getPeople().set(MPA_NAME, getPref(FIRST_NAME_FROM_SOCIAL_LOGIN, mUser.getFullName()));
                    mixPanel.getPeople().set(MPA_REFERRAL_CODE, mUser.getReferralCode());
                    mixPanel.getPeople().set(MPA_FRIEND_REFERRAL_CODE, mUser.getFriendReferralCode());
                    mixPanel.getPeople().set(MPA_IS_NOTIFICATION_ENABLED, NotificationsUtils.isNotificationEnabled(mContext));

                    if (getUnixTimeStampFromSED(mUser.getSubscriptionEndDate()) > 0) {

                        mixPanel.getPeople().set(MPA_SUBSCRIPTION_END_DATE, getUnixTimeStampFromSED(mUser.getSubscriptionEndDate()));

                    } else {
                        mixPanel.getPeople().set(MPA_SUBSCRIPTION_END_DATE, mUser.getSubscriptionEndDate());
                    }

                    if (isAfterFirstVideo) {

                        //Increment video play count
                        updateMixPanelPeopleAttribute(MPA_VIDEO_PLAY_COUNT, "1.0", true);

                    }

                }

                //This will send the questionnaire data only once when user come from Free Trial screen and is Indian User
                //Set Questionnaire data to mixpanel
                // Commented on 20 Dec 2018
                //setPeopleAttributeForQuestionnaire();

                //Initialisation of push notification
                initializeMixpanelPush();

                //Check whether an $email is available or not for mixpanel id
                //checkMixpanelEmail();


                 /*//This will send last session to mixpanel
                        SharedPrefrences mSharedPref = new SharedPrefrences(mContext);
                        try {
                            if (!mSharedPref.getPreferences(mSharedPref.LAST_APP_OPEN_DATE, "").equals("")) {

                                String strLastAppOpenDate = mSharedPref.getPreferences(mSharedPref.LAST_APP_OPEN_DATE, "");

                                mixPanel.getPeople().set(MPA_LAST_SESSION, strLastAppOpenDate);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }*/


            }
        } catch (Exception ex) {
            ErrorLog.SendErrorReportMixPanel(ex);
            ex.printStackTrace();
        }
    }

    /***
     * This will send the People attribute to mixpanel just after first video played after app update
     *
     * @param
     */

    public void sendMixPanelPeopleAfterAppUpdate() {
        try {

            if (mixPanel != null) {

                LoginUser mUser = new SharedPrefrences(mContext).getLoginUser();

                if (mUser != null) {

                    identifyPeopleUser(true);

                    mixPanel.getPeople().set(MPA_EMAIL, mUser.getEmail());

                    //Only login event for repeated login
                    //sendData(MPE_LOGIN, null);

                    //Default attributes updated with every people attribute time
                    mixPanel.getPeople().set(MPA_NAME, getPref(FIRST_NAME_FROM_SOCIAL_LOGIN, mUser.getFullName()));
                    mixPanel.getPeople().set(MPA_REGISTER_DATE, mUser.getCreatedDate());
                    mixPanel.getPeople().set(MPA_REFERRAL_CODE, mUser.getReferralCode());
                    mixPanel.getPeople().set(MPA_FRIEND_REFERRAL_CODE, mUser.getFriendReferralCode());

                    if (getUnixTimeStampFromSED(mUser.getSubscriptionEndDate()) > 0) {

                        mixPanel.getPeople().set(MPA_SUBSCRIPTION_END_DATE, getUnixTimeStampFromSED(mUser.getSubscriptionEndDate()));

                    } else {
                        mixPanel.getPeople().set(MPA_SUBSCRIPTION_END_DATE, mUser.getSubscriptionEndDate());
                    }

                }

            }
        } catch (Exception ex) {
            ErrorLog.SendErrorReportMixPanel(ex);
            ex.printStackTrace();
        }
    }

    /***
     * Update an attribute of a particular user after identifying the user
     *
     * @param attributeName
     * @param attributeValue
     */

    public void updateMixPanelPeopleAttribute(String attributeName, String attributeValue, boolean isIncrement) {

        try {

            if (mixPanel != null) {
                LoginUser mUser = new SharedPrefrences(mContext).getLoginUser();
                if (mUser != null) {

                    identifyPeopleUser(true);

                    if (!isIncrement) {

                        //To send date in time stamp
                        if (attributeName.equalsIgnoreCase(MPA_SUBSCRIPTION_END_DATE)) {

                            if (getUnixTimeStampFromSED(mUser.getSubscriptionEndDate()) > 0) {

                                mixPanel.getPeople().set(attributeName, getUnixTimeStampFromSED(attributeValue));

                            } else {
                                mixPanel.getPeople().set(attributeName, attributeValue);
                            }

                        } else {
                            mixPanel.getPeople().set(attributeName, attributeValue);
                        }


                    } else {

                        //To Print different distinct IDs
                        /*String Mixpanel_Alias_Value = getMixPanelAliasValue(false);
                        String People_Distinct_ID = mixPanel.getPeople().getDistinctId();
                        String Mixpanel_Distinct_ID = mixPanel.getDistinctId();*/

                        mixPanel.getPeople().increment(attributeName, Double.parseDouble(attributeValue));
                    }

                    mixPanel.getPeople().set(MPA_IS_NOTIFICATION_ENABLED, NotificationsUtils.isNotificationEnabled(mContext));

                }

            }

        } catch (Exception ex) {
            ErrorLog.SendErrorReportMixPanel(ex);
            ex.printStackTrace();
        }
    }

    /***
     * This is the distinct value of alias which will be sent to Mixpanel
     *
     * @param
     */

    public String getMixPanelAliasValue(boolean isPeopleProperty) {

        try {
            AppUtil mAppUtil = new AppUtil(mContext);

            if (mAppUtil != null) {
                return "" + mAppUtil.getDeviceID();
            }

        } catch (Exception ex) {
            ErrorLog.SendErrorReportMixPanel(ex);
            ex.printStackTrace();
        }

        return "";
    }

    /***
     * This will convert the SED to Unix Timestamp
     * See https://www.unixtimestamp.com/index.php for more info on UnixTimeStamp
     *
     * @param sed
     * @return
     */
    public long getUnixTimeStampFromSED(String sed) {

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            Date date = dateFormat.parse(sed);
            long unixTime = (long) date.getTime() / 1000;

            return unixTime;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;

    }

    /***
     * This will set people attribute for Questionnaire
     */
    public void setPeopleAttributeForQuestionnaire() {

        //getOtherOptionAnswer() will be used because every answer will be saved in this variable
        mixPanel.getPeople().set(MPA_USE, getQuestionPreference("Q1").getOtherOptionAnswer());
        mixPanel.getPeople().set(MPA_EXAM, getQuestionPreference("Q2").getOtherOptionAnswer());
        mixPanel.getPeople().set(MPA_YEAR, getQuestionPreference("Q3").getOtherOptionAnswer());


    }

    /***
     * This will update people attribute for Questionnaire only VP Exam and called from settings
     */
    public void updateQuestionnairePreference(String preference) {

        //getOtherOptionAnswer() will be used because every answer will be saved in this variable

        mixPanel.getPeople().set(MPA_EXAM, "" + preference);

    }

    /***
     * This will return Questionnaire model against saved shared preference json
     *
     * @param preferenceName
     * @return
     */
    public Questionnaire getQuestionPreference(String preferenceName) {

        Questionnaire mQuestionnaire = new Questionnaire();

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("Questionnaire", Context.MODE_PRIVATE);

        String questionJson = sharedPreferences.getString(preferenceName, "{}");

        try {

            JSONObject mJsonObject = new JSONObject(questionJson);
            mQuestionnaire = new Gson().fromJson(mJsonObject.toString(), Questionnaire.class);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return mQuestionnaire;

    }


    /***
     * @param pref
     * @param val
     */
    public void setPref(String pref, String val) {
        editor.putString(pref, val);
        editor.commit();

    }

    /***
     * @param pref
     * @param val
     * @return
     */
    public String getPref(String pref, String val) {
        return sharedPref.getString(pref,
                val);
    }

    /***
     * @param pref
     * @param val
     */
    public void setPref(String pref, Integer val) {
        editor.putInt(pref, val);
        editor.commit();

    }

    /***
     * @param pref
     * @param val
     * @return
     */
    public Integer getPref(String pref, Integer val) {
        return sharedPref.getInt(pref,
                val);
    }

    /***
     * @param pref
     * @param val
     */
    public void setPref(String pref, Boolean val) {

        editor.putBoolean(pref, val);
        editor.commit();

    }

    /***
     * @param pref
     * @param val
     * @return
     */
    public Boolean getPref(String pref, Boolean val) {
        return sharedPref.getBoolean(pref,
                val);
    }

    /***
     * @param
     */
    public void clearMixPanelPreference() {

        editor.clear();
        editor.commit();

    }

    /*********CODE TO CHECK MIXPANEL EMAIL ID*******/

    /***
     * Check if email property on Mixpanel is available or not; Calling is commented
     */
    private void checkMixpanelEmail() {

        LoginUser mUser = new SharedPrefrences(mContext).getLoginUser();

        if (mUser != null) {

            if (!getPref(MixPanelClass.IS_EMAIL_CHECKED_ON_MIXPANEL, false)) {

                if (mApiCall != null) {
                    mApiCall.isEmailAvailableOnMixpanel(new OnTaskCompleted(this), mApiCall.IsEmailAvailableOnMixpanel, mUser.getStudentID(), getMixPanelAliasValue(false), mixPanel.getDistinctId(), mixPanel.getPeople().getDistinctId());
                }
                setPref(MixPanelClass.IS_EMAIL_CHECKED_ON_MIXPANEL, true);
            }
        }
    }


    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {

        if (Method.equals(mApiCall.IsEmailAvailableOnMixpanel)) {
            parseResponseForIsEmailAvailable(result);
        }
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {

    }


    /***
     * Parse the response to check whether an email id is available for device id
     *
     * @param response
     */
    private void parseResponseForIsEmailAvailable(JSONObject response) {

        try {

            JSONObject mJsonObj = response.getJSONObject(mApiCall.IsEmailAvailableOnMixpanel);

            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1) {

            } else if (ErrorCode == 2) {

            } else if (ErrorCode == 0) {

                LoginUser mUser = new SharedPrefrences(mContext).getLoginUser();

                if (mUser != null) {

                    identifyPeopleUser(true);
                    //Set Email ID o Mixpanel
                    mixPanel.getPeople().set(MPA_EMAIL, mUser.getEmail());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /***
     * This will determine if the saved TagID is valid or not, that is 1,2,3,4
     * ""(Blank)--> means user has not played questionnaire yet
     * 0--> other option is selected
     *
     * @return
     */
    public boolean isValidSavedTagID() {

        LoginUser mUser = new SharedPrefrences(mContext).getLoginUser();

        if (mUser != null) {

            String savedTagID = mUser.getTagID();

            if (savedTagID != null) {
                if (savedTagID.trim().length() > 0) {
                    if (!savedTagID.equalsIgnoreCase("") && !savedTagID.equalsIgnoreCase("0")) {
                        return true;
                    }
                }
            }
        }

        return false;

    }

    /***
     * This will determine if the saved TagID is valid or not that is 1,2,3
     * ""(Blank)--> means user has not played questionnaire yet
     * 4--> other option is selected
     * 5--> If answer not available means blank and user has played questionnaire
     *
     * @return
     */
    public boolean isQuestionnaireAnswered() {

        LoginUser mUser = new SharedPrefrences(mContext).getLoginUser();

        if (mUser != null) {

            String savedTagID = mUser.getTagID();

            if (savedTagID != null) {
                if (savedTagID.trim().length() > 0) {
                    if (!savedTagID.equalsIgnoreCase("")) {
                        return true;
                    }
                }
            }
        }

        return false;

    }

    /***
     * This will return the TagID from SP
     *
     * @return
     */
    public String getTagID() {

        if (isValidSavedTagID()) {

            LoginUser mUser = new SharedPrefrences(mContext).getLoginUser();

            return mUser.getTagID();
        }

        return MixPanelClass.DEFAULT_TAG_ID;

    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    private void sendFacebookEvent(String trackingKey/*Event Name*/, HashMap<String, String> hashMap) {

        try {

            AppEventsLogger logger = AppEventsLogger.newLogger(mContext);
            Bundle params = new Bundle();

            //With no property values
            if (hashMap != null) {

                //Get the set
                Set set = (Set) hashMap.entrySet();

                //Create iterator on Set
                Iterator iterator = set.iterator();

                while (iterator.hasNext()) {
                    Map.Entry mapEntry = (Map.Entry) iterator.next();

                    // Get Key
                    String keyValue = (String) mapEntry.getKey();

                    //Get Value
                    String value = (String) mapEntry.getValue();

                    try {

                        //Set params through a loop
                        params.putString(keyValue, value);

                    } catch (Exception ex) {

                        ex.printStackTrace();

                    }
                }

                //Call logger to send data with properties
                logger.logEvent(trackingKey, params);

            } else {

                //Call logger to send data without properties
                logger.logEvent(trackingKey, params);
            }


        } catch (Exception ex) {
            ErrorLog.SendErrorReportMixPanel(ex);
            ex.printStackTrace();
        }

    }

    /***
     * This will return true if a particular event wanted to send on FB
     *
     * @return
     */
    private boolean isSendOnFacebook(String trackingKey /*Event Name*/) {

        switch (trackingKey) {
            case MPE_BOUGHT:
                return true;
            case MPE_PAYMENT_ABANDONED:
                return true;
        }

        return false;
    }

}
