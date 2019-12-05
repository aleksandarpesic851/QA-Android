package deeplink;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import Utils.SharedPrefrences;

/**
 * Created by iziss on 11/10/18.
 */
public class DeepLinkGenerator {

    public static final String DYNAMIC_LINK_DOMAIN = "n88nw.app.goo.gl";//Unique link for VP
    public static final String PARSER_REFERRAL_CODE = "http://www.visualphysics.nlytn.in/referralCode/";
    public static final String PARSER_VIDEO = "http://www.visualphysics.nlytn.in/playvideo/";
    public static final String ANDROID_PARAMETERS = "com.visualphysics";

    /*Keys to store deep links*/
    public static final String PREF_REFERRAL_DEEP_LINK = "prefDeepLink";
    public static final String PREF_VIDEO_PREFIX = "Vid_";
    public static final String DEFAULT_DEEP_LINK = "";

    private OnLinkGenerateListener mOnLinkGenerateListener;
    private Activity mActivity;

    //Preference name and other references
    private static final String MIX_PANEL_PREFERENCE = "DeepLink_VP_Pref";
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private SharedPrefrences mSharedPref;

    /*Default constructor to initialize*/
    public DeepLinkGenerator(Activity mActivity) {
        this.mActivity = mActivity;

        //Initialize shared preference
        sharedPref = mActivity.getSharedPreferences(MIX_PANEL_PREFERENCE, Context.MODE_PRIVATE);

        //get editor to put values
        editor = sharedPref.edit();
    }

    /*Default constructor to initialize with Context object*/
    public DeepLinkGenerator(Context mContext) {

        //Initialize shared preference
        sharedPref = mContext.getSharedPreferences(MIX_PANEL_PREFERENCE, Context.MODE_PRIVATE);

        //get editor to put values
        editor = sharedPref.edit();
    }

    /*Set call back listener to get the link*/
    public void setCallBack(OnLinkGenerateListener mOnLinkGenerateListener) {
        this.mOnLinkGenerateListener = mOnLinkGenerateListener;
    }

    /**
     * This will generate or provide saved referral deep link
     *
     * @param
     */
    public void getDeepLinkForReferralCode() {

        /*Will get value from local from this SP*/
        mSharedPref = new SharedPrefrences(mActivity);


        String storedDeepLink = getPref(PREF_REFERRAL_DEEP_LINK, DEFAULT_DEEP_LINK);

        if (storedDeepLink.equalsIgnoreCase(DEFAULT_DEEP_LINK)) {

            /*This will generate and save deep link to SP, parameters description
            /*1--> Type of link
            * 2--> Value to be passed in as value in deep link
            * 3--> Link parser which will generate a link
            * 4 --> Static Preference Key */
            generateDeepLinkFromFirebase(DeepLinkType.REFERRAL_LINK, PARSER_REFERRAL_CODE, mSharedPref.getLoginUser().getReferralCode(), PREF_REFERRAL_DEEP_LINK);

        } else {
            if (mOnLinkGenerateListener != null) {
                mOnLinkGenerateListener.onSuccess(storedDeepLink);
            }
        }

    }

    /**
     * This will generate ot return saved deep link for each video
     *
     * @param chapterID
     * @param videoID
     */
    public void getDeepLinkForVideo(String chapterID, String videoID) {

        String storedDeepLink = getPref(PREF_VIDEO_PREFIX + videoID, DEFAULT_DEEP_LINK);

        if (storedDeepLink.equalsIgnoreCase(DEFAULT_DEEP_LINK)) {

            /*This will generate and save deep link to SP, parameters description*/
            /*1--> Type of link
            * 2--> Value to be passed in as value in deep link
            * 3--> Link parser which will generate a link
            * 4 --> Dynamic Preference Key as per VideoID*/
            generateDeepLinkFromFirebase(DeepLinkType.VIDEO, PARSER_VIDEO, chapterID + "/" + videoID, PREF_VIDEO_PREFIX + videoID);

        } else {
            if (mOnLinkGenerateListener != null) {
                mOnLinkGenerateListener.onSuccess(storedDeepLink);
            }
        }

    }

    /**
     * This will generate a deep link and save it in SP
     *
     * @param deepLinkType
     * @param parserUrl
     * @param valueForDeepLink
     */
    private void generateDeepLinkFromFirebase(final DeepLinkType deepLinkType, String parserUrl, String valueForDeepLink, final String prefKey) {

        /**
         * Facing issue with short dynamic links and updation of library gives issue
         * https://stackoverflow.com/questions/52152116/short-dynamic-link-error-com-google-android-gms-common-api-apiexception-8
         */
        DynamicLink longDynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(parserUrl + valueForDeepLink))
                .setDynamicLinkDomain(DYNAMIC_LINK_DOMAIN)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(ANDROID_PARAMETERS)
                        .build())
                .buildDynamicLink();

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(longDynamicLink.getUri())
                .buildShortDynamicLink()
                .addOnCompleteListener(mActivity, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            try {

                                // Short link created
                                Uri shortLink = task.getResult().getShortLink();

                                /*Save to SP*/
                                setPref(prefKey, shortLink.toString());

                                if (mOnLinkGenerateListener != null) {
                                    mOnLinkGenerateListener.onSuccess(shortLink.toString());
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        } else {

                            if (mOnLinkGenerateListener != null) {
                                mOnLinkGenerateListener.onFail(task.getException().toString());
                            }
                        }
                    }
                });

/*Commented as giving issue*/
/*        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(parserUrl + valueForDeepLink))
                .setDynamicLinkDomain(DYNAMIC_LINK_DOMAIN)

                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.visualphysics").build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnCompleteListener(mActivity, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            try {
                                // Short link created
                                Uri shortLink = task.getResult().getShortLink();

                                //This will generate long url
                                //Uri flowchartLink = task.getResult().getPreviewLink();

                                *//*Save to SP*//*
                                setPref(prefKey, shortLink.toString());

                                if (mOnLinkGenerateListener != null) {
                                    mOnLinkGenerateListener.onSuccess(shortLink.toString());
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        } else {

                            if (mOnLinkGenerateListener != null) {
                                mOnLinkGenerateListener.onFail(task.getException().toString());
                            }
                        }
                    }
                });

        *//*DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("http://www.visualphysics.nlytn.in/referralCode/" + referralCode))
                .setDynamicLinkDomain("https://n88nw.app.goo.gl/")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildDynamicLink();*/

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
     * @param
     */
    public void clearMixPanelPreference() {
        editor.clear();
        editor.commit();

    }

}
