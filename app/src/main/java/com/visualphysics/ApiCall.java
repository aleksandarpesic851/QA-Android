package com.visualphysics;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import Utils.AppUtil;
import Utils.ApplicationConfiguration;
import Utils.DebugLog;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;

/**
 * This File is used for calling Web service
 */
public class ApiCall {

    private ApplicationConfiguration mAppConfig;

    private Context mContext;

    /*Production URL*/
    //public static String DOMAIN_ROOT_NAME = "http://visualphysics.nlytn.in/admin";

    /*Testing URL*/
    public static String DOMAIN_ROOT_NAME = "https://vptest.nlytn.in/admin";

    /*Testing Production URL*/
    //public static String DOMAIN_ROOT_NAME = "http://prodtest.nlytn.in/admin";

     /*Old Testing Production URL*/
    //public static String DOMAIN_ROOT_NAME = "https://vpt.nlytn.in/admin";

    private String DOMAIN_NAME = DOMAIN_ROOT_NAME + "/api/service";

    //This will send a response with country according to IP address
    private String API_URL_FOR_LOCATION = "http://www.ip-api.com/json";
    public String GetLocation = "getLocation";

    //Method Name
    public String ForgotPassword = "forgotPassword";
    public String ChangePassword = "changePassword";
    public String DeleteAccount = "deleteAccount";
    public String GetCategories = "listCategories";
    public String CheckOtpNumber = "checkOTPVerification";
    public String GetOtpNumber = "generateOTP";
    public String GetAllChapters = "getAllChapter";
    public String GetChapterVideos = "getAllVideo";
    public String SignUp = "studentSignUp";
    public String Login = "checkLogin";
    public String AppMenu = "appMenu";
    public String CheckUpdates = "checkLastUpdates";
    public String GetAllChapterAndVideos = "getAllChapterVideo";
    public String LoginWithSocialMedia = "socialMediaLogin";
    public String AskToExpertList = "listAskToExpert";
    public String AddCommentAskToExpert = "commentAskToExpert";
    public String GetDiscussionAskToExpert = "discussionAskToExpert";
    public String AddMessageToDiscussionAskToExpert = "addResponseAskToExpert";
    public String AddVideoLike = "addVideoLike";
    public String AddPendingVideoDetail = "addVideoActivityCount";
    public String AddVideoDownloadCount = "addVideoDownload";
    public String AddVideoPlayCount = "addVideoPlay";
    public String GetLicenseToken = "getLicenceToken";
    public String GetPackages = "getPackage";
    public String GetServerTime = "getTime";
    public String GetUserSubscription = "getStudentSubcription";
    public String UpdateLicenseTokenAcquire = "updateDeviceLicence";
    public String AddSubscriptionByCoupon = "addStudentSubcriptionByCoupon";
    public String AddReferalCode = "addFriendRefer";
    public String DelinkDevice = "delinkStudentAccount";
    public String GetSearchKeyword = "getSearchKeyword";
    public String PostSearchKeyword = "postSearchKeyword";

    //For Questionnaire data
    public String SendQuestionnaireData = "sendQuestionnaireData";

    //To Check whether a email is sent to mixpanel and is available
    public String IsEmailAvailableOnMixpanel = "isEmailAvailableOnMixpanel";

    //To redirect the API call from http to https
    public final String HTTP = "http:";
    public final String HTTPS = "https:";

    private JSONObject mParentData;
    private JSONObject mChildData;


    public ApiCall() {
    }

    public ApiCall(Context mContext) {
        mAppConfig = new ApplicationConfiguration(mContext);
        this.mContext = mContext;
    }


    /***
     * Change Password
     *
     * @param Email      Email
     * @param listener   Callback Listener
     * @param MethodName Callback Method name
     */
    public void doForgotPassword(String Email, OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", ForgotPassword);
            mChildData.put("EmailId", Email);

            mParentData.put("body", mChildData);
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

//    public void getChapterVideo(String DeviceId, int ChapterId, OnTaskCompleted listener, String MethodName){
//        try {
//            mParentData = new JSONObject();
//            mChildData = new JSONObject();
//
//            mParentData.put("method", MethodName);
//            mChildData.put("DeviceToken", DeviceId);
//            mChildData.put("ChapterID",ChapterId );
//            mChildData.put("CurrentPage",1);
//            mChildData.put("PageSize",10);
//
//            mParentData.put("body", mChildData);
//            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters
//
//        } catch (Exception e) {
//            // TODO: handle exception
//            e.printStackTrace();
//        }
//    }

    /***
     * Get OTP
     *
     * @param studentID
     * @param listener
     * @param MethodName
     */
    public void getOtpNumber(String studentID, OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", studentID);

            mParentData.put("body", mChildData);
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Check OTP
     *
     * @param studentID
     * @param otpNumber
     * @param listener
     * @param MethodName
     */

    public void checkOtpNumber(String studentID, String otpNumber, String DeviceTokenID,
                               OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", studentID);
            mChildData.put("DeviceTokenID", DeviceTokenID);
            mChildData.put("OTP", otpNumber);

            mParentData.put("body", mChildData);
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Change Login user password
     *
     * @param StudentID
     * @param NewPassword
     * @param OldPassword
     * @param listener
     * @param MethodName
     */
    public void doChangePassword(String StudentID, String NewPassword, String OldPassword,
                                 OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", ChangePassword);
            mChildData.put("StudentID", StudentID);
            mChildData.put("OldPassword", OldPassword);
            mChildData.put("Password", NewPassword);

            mParentData.put("body", mChildData);
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Delete Account
     *
     * @param StudentID
     * @param listener
     * @param MethodName
     */
    public void doDeleteAccount(String StudentID,
                                OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", DeleteAccount);
            mChildData.put("StudentID", StudentID);

            mParentData.put("body", mChildData);
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * POST All SearchKeywords data
     *
     * @param DeviceToken
     * @param listener
     * @param MethodName
     */
    public void postSearchKeywords(String DeviceToken,
                                  OnTaskCompleted listener, String MethodName, JSONArray searchKeywordArray) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", PostSearchKeyword);
            mChildData.put("DeviceToken", DeviceToken);
            mChildData.put("Keywords", searchKeywordArray);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Get All Category == " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Get All SearchKeywords
     *
     * @param DeviceToken
     * @param listener
     * @param MethodName
     */
    public void getSearchKeywords(String DeviceToken,
                              OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", GetSearchKeyword);
            mChildData.put("DeviceToken", DeviceToken);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Get All Category == " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Get All Category
     *
     * @param DeviceToken
     * @param listener
     * @param MethodName
     */
    public void getCategories(String DeviceToken,
                              OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", GetCategories);
            mChildData.put("DeviceToken", DeviceToken);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Get All Category == " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Get All chapters of all category
     *
     * @param DeviceToken
     * @param listener
     * @param MethodName
     */
    public void getAllChapters(String DeviceToken,
                               OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", GetAllChapters);
            mChildData.put("DeviceToken", DeviceToken);

            mParentData.put("body", mChildData);
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Get all videos realted selected chapter
     *
     * @param DeviceToken
     * @param ChapterID
     * @param listener
     * @param MethodName
     */
    public void getChapterVideos(String DeviceToken, int ChapterID,
                                 OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", GetChapterVideos);
            mChildData.put("DeviceToken", DeviceToken);
            mChildData.put("ChapterID", ChapterID);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Get All Video == " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * @param FullName
     * @param Email
     * @param Password
     * @param Cellphone
     * @param CountryID
     * @param DeviceName
     * @param DeviceOS
     * @param OSVersion
     * @param DeviceID
     * @param RegType
     * @param RegId
     * @param FriendReferralCode
     * @param AppVersion
     * @param BuildSerialNumber
     * @param listener
     * @param MethodName
     */
    public void doSignUp(String FullName, String Email, String Password, String Cellphone, String CountryID,
                         String DeviceName, String DeviceOS, String OSVersion, String DeviceID, String RegType,
                         String RegId, String FriendReferralCode, String AppVersion, String BuildSerialNumber,
                         String DeviceType,
                         OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", SignUp);
            mChildData.put("FullName", FullName);
            mChildData.put("Email", Email);
            mChildData.put("Password", Password);
            mChildData.put("Cellphone", Cellphone);
            mChildData.put("CountryID", CountryID);
            mChildData.put("DeviceName", DeviceName);
            mChildData.put("DeviceOS", DeviceOS);
            mChildData.put("OSVersion", OSVersion);
            mChildData.put("DeviceTokenID", DeviceID);
            mChildData.put("RegType", RegType);
            mChildData.put("RegId", RegId);
            mChildData.put("FriendReferralCode", FriendReferralCode);
            mChildData.put("AppVersion", AppVersion);
            mChildData.put("BuildSerialNumber", BuildSerialNumber);
            mChildData.put("DeviceType", DeviceType);

            mParentData.put("body", mChildData);
            DebugLog.d("", "SignUP Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Do Login
     *
     * @param Email
     * @param Password
     * @param DeviceName
     * @param DeviceOS
     * @param OSVersion
     * @param DeviceID
     * @param AppVersion
     * @param BuildSerialNumber
     * @param listener
     * @param MethodName
     */
    public void doLogin(String Email, String Password, String DeviceName, String DeviceOS,
                        String OSVersion, String DeviceID, String AppVersion, String BuildSerialNumber,
                        String DeviceType,
                        OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);

            mChildData.put("Email", Email);
            mChildData.put("Password", Password);
            mChildData.put("DeviceName", DeviceName);
            mChildData.put("DeviceOS", DeviceOS);
            mChildData.put("OSVersion", OSVersion);
            mChildData.put("DeviceTokenID", DeviceID);
            mChildData.put("AppVersion", AppVersion);
            mChildData.put("BuildSerialNumber", BuildSerialNumber);
            mChildData.put("DeviceType", DeviceType);

            mParentData.put("body", mChildData);
            DebugLog.d("", "SignUP Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Get the applicaiton Menu that not to display in APP
     *
     * @param DeviceID   Device Token
     * @param listener   Callback Lisetener
     * @param MethodName Method Name
     */
    public void getAppMenu(String DeviceID,
                           OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("DeviceToken", DeviceID);

            mParentData.put("body", mChildData);
            DebugLog.d("", "AppMenu Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Get the latest Update on the bases of the last sync date
     *
     * @param LastSyncDate
     * @param listener
     * @param MethodName
     */
    public void getLatestUpdate(String UserID, String DeviceID, String LastSyncDate, String CurrentUTC,
                                OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("LastDeviceSync", LastSyncDate);
            mChildData.put("CurrentDeviceUTC", CurrentUTC);
            mChildData.put("StudentID", UserID);
            mChildData.put("DeviceToken", DeviceID);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Get Latest Update and Sycn with server === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * @param DeviceID
     * @param listener
     * @param MethodName
     */
    public void getAllChaptersAndAllVideos(String DeviceID,
                                           OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("DeviceToken", DeviceID);

            mParentData.put("body", mChildData);
            DebugLog.d("", "AppMenu Data === " + mParentData.toString());

            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Login with Social Media
     *
     * @param FullName
     * @param Email
     * @param CountryID
     * @param DeviceName
     * @param DeviceOS
     * @param OSVersion
     * @param DeviceID
     * @param RegType
     * @param RegId
     * @param FriendReferralCode
     * @param AppVersion
     * @param BuildSerialNumber
     * @param DeviceType
     * @param listener
     * @param MethodName
     */
    public void doLoginWithSocialMedia(String FullName, String Email, String CountryID,
                                       String DeviceName, String DeviceOS, String OSVersion, String DeviceID, String RegType,
                                       String RegId, String FriendReferralCode, String AppVersion, String BuildSerialNumber,
                                       String DeviceType,
                                       OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("FullName", FullName);
            mChildData.put("Email", Email);
            mChildData.put("CountryID", CountryID);
            mChildData.put("DeviceName", DeviceName);
            mChildData.put("DeviceOS", DeviceOS);
            mChildData.put("OSVersion", OSVersion);
            mChildData.put("DeviceTokenID", DeviceID);
            mChildData.put("RegType", RegType);
            mChildData.put("RegId", RegId);
            mChildData.put("FriendReferralCode", FriendReferralCode);
            mChildData.put("AppVersion", AppVersion);
            mChildData.put("BuildSerialNumber", BuildSerialNumber);
            mChildData.put("DeviceType", DeviceType);

            mParentData.put("body", mChildData);
            DebugLog.d("", "SignUP Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Login With Social Media when user Add email manually so at that time OtpCheckAllow is need to pass 2 so that
     * server get know that we have to verfiy user with OTP
     *
     * @param FullName
     * @param Email
     * @param CountryID
     * @param DeviceName
     * @param DeviceOS
     * @param OSVersion
     * @param DeviceID
     * @param RegType
     * @param RegId
     * @param FriendReferralCode
     * @param AppVersion
     * @param BuildSerialNumber
     * @param DeviceType
     * @param OtpCheckAllow
     * @param listener
     * @param MethodName
     */
    public void doLoginWithSocialMedia(String FullName, String Email, String CountryID,
                                       String DeviceName, String DeviceOS, String OSVersion, String DeviceID, String RegType,
                                       String RegId, String FriendReferralCode, String AppVersion, String BuildSerialNumber,
                                       String DeviceType, int OtpCheckAllow,
                                       OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("FullName", FullName);
            mChildData.put("Email", Email);
            mChildData.put("CountryID", CountryID);
            mChildData.put("DeviceName", DeviceName);
            mChildData.put("DeviceOS", DeviceOS);
            mChildData.put("OSVersion", OSVersion);
            mChildData.put("DeviceTokenID", DeviceID);
            mChildData.put("RegType", RegType);
            mChildData.put("RegId", RegId);
            mChildData.put("FriendReferralCode", FriendReferralCode);
            mChildData.put("AppVersion", AppVersion);
            mChildData.put("BuildSerialNumber", BuildSerialNumber);
            mChildData.put("DeviceType", DeviceType);
            mChildData.put("OtpCheckAllow", OtpCheckAllow);

            mParentData.put("body", mChildData);
            DebugLog.d("", "SignUP Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Get the list of Ask to Expert list
     *
     * @param PageSize
     * @param CurrentPage
     * @param listener
     * @param MethodName
     */
    public void getAskToExpertList(int PageSize, int CurrentPage,
                                   OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("PageSize", PageSize);
            mChildData.put("CurrentPage", CurrentPage);

            mParentData.put("body", mChildData);
            DebugLog.d("", "AppMenu Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Ask the Question to Expert
     *
     * @param StudentID
     * @param Title
     * @param Comment
     * @param listener
     * @param MethodName
     */
    public void doAddCommentAskToExpert(String StudentID, String Title, String Comment,
                                        OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", StudentID);
            mChildData.put("Title", Title);
            mChildData.put("Comment", Comment);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Add Comment Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Get the discussion of Ask to
     *
     * @param AskToExpertID
     * @param listener
     * @param MethodName
     */
    public void getDiscussionOfAskToExpertList(String AskToExpertID,
                                               OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("AskToExpertID", AskToExpertID);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Discussion of ask to expert Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Add Message to Dissucssion To Ask to Expert to particular question
     *
     * @param UserID
     * @param UserType
     * @param Comment
     * @param AskToExpertID
     * @param listener
     * @param MethodName
     */
    public void doAddMessageToAskToExpertDiscussion(String UserID, String UserType, String Comment, String AskToExpertID,
                                                    OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("UserID", UserID);
            mChildData.put("UserType", UserType);
            mChildData.put("Comment", Comment);
            mChildData.put("AskToExpertID", AskToExpertID);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Discussion of ask to expert Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Add Video Like
     *
     * @param VideoID    Video ID
     * @param UserID     Login User ID
     * @param DeviceID   Device Token
     * @param listener   Callback listener
     * @param MethodName Method Name
     */
    public void doAddVideoLike(int VideoID, String UserID, String DeviceID,
                               OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("VideoID", VideoID);
            mChildData.put("StudentID", UserID);
            mChildData.put("DeviceToken", DeviceID);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Add Video Like Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /****
     * Update Pending Video Detail
     *
     * @param UserID
     * @param DeviceID
     * @param VideoDetailArray
     * @param listener
     * @param MethodName
     */
    public void doUpdatePendingVideoDetail(String UserID, String DeviceID, JSONArray VideoDetailArray,
                                           OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", UserID);
            mChildData.put("DeviceToken", DeviceID);
            mChildData.put("VideoSyncArr", VideoDetailArray);

            mParentData.put("body", mChildData);
            Log.d("", "Add Video Like Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Add Video Download count
     *
     * @param VideoID
     * @param UserID
     * @param DeviceID
     * @param listener
     * @param MethodName
     */
    public void doAddVideoDownloadCount(int VideoID, String UserID, String DeviceID,
                                        OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("VideoID", VideoID);
            mChildData.put("StudentID", UserID);
            mChildData.put("DeviceToken", DeviceID);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Add Video Download count Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Add Video Play count
     *
     * @param VideoID
     * @param UserID
     * @param DeviceID
     * @param listener
     * @param MethodName
     */
    public void doAddVideoPlayCount(int VideoID, String UserID, String DeviceID,
                                    OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("VideoID", VideoID);
            mChildData.put("StudentID", UserID);
            mChildData.put("DeviceToken", DeviceID);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Add Video Download count Data === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Get the New License
     *
     * @param UserID
     * @param listener
     * @param MethodName
     */
    public void getLicenseToken(String UserID, String DeviceToken, String CurrentUTC,
                                OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", UserID);
            mChildData.put("CurrentDeviceUTC", CurrentUTC);
            mChildData.put("DeviceToken", DeviceToken);
            mParentData.put("body", mChildData);
            DebugLog.d("", "Get Device Token === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Get the Packages for Subscription
     *
     * @param listener
     * @param MethodName
     */
    public void getPackage(OnTaskCompleted listener, String MethodName, String categoryPackageID) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);

            //Added by Iziss to categorised the packages
            mChildData.put("packageCategoryID", categoryPackageID);


            mParentData.put("body", mChildData);
            DebugLog.d("", "Get Get Package === " + mParentData.toString());

            //Toast.makeText(mContext, mParentData.toString(), Toast.LENGTH_LONG).show();

            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Get the Service UTC date time
     *
     * @param listener
     * @param MethodName
     */
    public void getServerTime(OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Get Get ServerTime === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Get User Subscription End date
     *
     * @param UserID
     * @param DeviceToken
     * @param listener
     * @param MethodName
     */
    public void getUserSubscription(String UserID, String DeviceToken,
                                    OnTaskCompleted listener, String MethodName) {
        try {

            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", UserID);
            mChildData.put("DeviceToken", DeviceToken);
            mParentData.put("body", mChildData);
            DebugLog.d("", "Get Get Susbscription === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***
     * Update License token
     *
     * @param UserID
     * @param DeviceToken
     * @param DaysLicence
     * @param AppVersion
     * @param DeviceName
     * @param DeviceOS
     * @param OSVersion
     * @param listener
     * @param MethodName
     */
    public void updateLicenseTokenAcquireSuccessfully(String UserID, String DeviceToken, String DaysLicence,
                                                      String AppVersion, String DeviceName, String DeviceOS, String OSVersion,
                                                      OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", UserID);
            mChildData.put("DeviceToken", DeviceToken);
            mChildData.put("DaysLicence", DaysLicence);
            mChildData.put("vAppVersion", AppVersion);
            mChildData.put("DeviceName", DeviceName);
            mChildData.put("DeviceOS", DeviceOS);
            mChildData.put("OSVersion", OSVersion);
            mParentData.put("body", mChildData);
            DebugLog.d("", "Get Get UpdateLicenseTokenAcquire === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * Add Sub by coupon code
     *
     * @param UserID
     * @param CouponID
     * @param listener
     * @param MethodName
     */
    public void addAddSubscriptionByCoupon(String UserID, String CouponID, String DeviceToken,
                                           OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", UserID);
            mChildData.put("DeviceToken", DeviceToken);
            mChildData.put("CouponID", CouponID);
            mParentData.put("body", mChildData);
            DebugLog.d("", "Add sub by coupon === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Add ReferalCode
     *
     * @param UserID
     * @param ReferalCode
     * @param listener
     * @param MethodName
     */
    public void doAddReferalCode(String UserID, String ReferalCode,
                                 OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", UserID);
            mChildData.put("FriendReferralCode", ReferalCode);
            mParentData.put("body", mChildData);
            DebugLog.d("", "Add Referal Code === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /***
     * @param UserID
     * @param DeviceToken
     * @param listener
     * @param MethodName
     */
    public void delinkDevice(String UserID, String DeviceToken,
                             OnTaskCompleted listener, String MethodName) {
        try {
            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);
            mChildData.put("StudentID", UserID);
            mChildData.put("DeviceToken", DeviceToken);
            mParentData.put("body", mChildData);
            DebugLog.d("", "Delink Device === " + mParentData.toString());
            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


/**********************************************************************************************/

    /***
     * Send JsonObject Request with Get Method
     *
     * @param Url        Api URL
     * @param listener   Callback Listener
     * @param MethodName Callback Listener
     */
    private void sendRequestWithGetData(String Url, final OnTaskCompleted listener, final String MethodName) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, Url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub
                // parseResponseData(response); // Parse Data
                listener.onTaskCompleted(response, MethodName);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                listener.onError(error, MethodName);
            }
        });
        MyApplication.getInstance().addToRequestQueue(jsObjRequest);
    }

    /***
     * Send String Request with Get Method
     *
     * @param Url        Api URL
     * @param listener   Callback Listener
     * @param MethodName Callback Method name
     */
    private void sendRequestWithGetDataString(String Url, final OnTaskCompleted listener, final String MethodName) {

        //JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, Url, null, new Response.Listener<JSONObject>() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        listener.onTaskCompleted(response, MethodName);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error, MethodName);
            }
        });
        MyApplication.getInstance().addToRequestQueue(stringRequest);
    }

    /***
     * Send JsonObject Request with Post Method
     *
     * @param Url        Api URL
     * @param jsonParams Paramter
     * @param listener   Callback Listener
     * @param MethodName Callback Method name
     */
    private void sendRequestWithPostData(final String Url, final JSONObject jsonParams, final OnTaskCompleted listener, final String MethodName) {

        //Encrypt Request
        JSONObject JsonObjParameter = EncrpytData(jsonParams);
        Log.i("", "Encrpt Data == " + JsonObjParameter.toString());

        SplashScreenActivity.sendCustomLog("Encrypted Req " + MethodName + ":" + JsonObjParameter.toString());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, Url, JsonObjParameter, response -> {
            // TODO Auto-generated method stub
            // parseResponseData(response); // Parse Data
            JSONObject mJsonObj = null;
            try {
                if (!MethodName.equals(GetLicenseToken)) {
                    //If there is error in encrytion then don't decrypt
                    if (response.has("encryption_error")) {
                        mJsonObj = response;

                        SplashScreenActivity.sendCustomLog("Encryption error " + MethodName + ":" + mJsonObj.toString());

                    } else {
                        String Data = response.getString("auth");
                        String Response = AppUtil.decrypt(Data.getBytes());

                        SplashScreenActivity.sendCustomLog("Decrypted Response " + MethodName + ":" + Response.toString());

                        Response = Response.replace("\\", "");
                        mJsonObj = new JSONObject(Response);
                        DebugLog.v("", "sendRequestWithPostData decrpt = " + response.toString());
                        DebugLog.v("", "sendRequestWithPostData = " + mJsonObj.toString());

                        SplashScreenActivity.sendCustomLog("Done Decrypted Response " + MethodName + ":" + mJsonObj.toString());
                    }
                } else {
                    DebugLog.v("", "sendRequestWithPostData decrpt = " + response.toString());
                    mJsonObj = response;

                    SplashScreenActivity.sendCustomLog("Token Response " + MethodName + ":" + mJsonObj.toString());

                }
            } catch (Exception e) {
                ErrorLog.SaveErrorLog(e);

            } finally {
                listener.onTaskCompleted(mJsonObj, MethodName);
            }

        }, error -> {
            // TODO Auto-generated method stub
            DebugLog.v("", "sendRequestWithPostData = " + error.toString());

            /*If the API gives error then we will check if the API is not for location, because it will not work with https
            * Rest of the APIs will redirect if they contain http in the URL to https. The method is recursively called just
            * after replacing the http with https and passed in the same method*/
            if (!MethodName.equals(GetLocation)) {
                if (!Url.contains(HTTPS)) {

                    String newUrl = Url.replace(HTTP, HTTPS);
                    sendRequestWithPostData(newUrl, jsonParams, listener, MethodName);

                } else {
                    listener.onError(error, MethodName);
                }

            } else {
                listener.onError(error, MethodName);
            }

        })

                //Added by IZISS Header
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 90000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        MyApplication.getInstance().addToRequestQueue(jsObjRequest);
    }

    /***
     * Send String Request with Post Method
     *
     * @param Url        Api URL
     * @param requestObj Parameter in Json object
     * @param listener   Callback Listener
     * @param MethodName Callback Method name
     */
    private void sendRequestWithPostDataString(String Url, JSONObject requestObj, final OnTaskCompleted listener, final String MethodName) {

        //JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, Url, null, new Response.Listener<JSONObject>() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.v("", "" + MethodName + " == " + response);
                        listener.onTaskCompleted(response, MethodName);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error, MethodName);
            }
        });
        /*int socketTimeout = 90000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);*/
        MyApplication.getInstance().addToRequestQueue(stringRequest);
    }

    /**
     * Encrypt the Request
     *
     * @param Request
     * @return
     */
    private JSONObject EncrpytData(JSONObject Request) {
        JSONObject JsonDataData = new JSONObject();
        try {
            JsonDataData.put("auth", AppUtil.encrypt(Request.toString().trim()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return JsonDataData;
        }
    }

    /**
     * For IP address
     *
     */

    /***
     * Send JsonObject Request with Get Method
     *
     * @param listener   Callback Listener
     * @param MethodName Callback Listener
     */
    public void getLocationFromIpAddress(final OnTaskCompleted listener, final String MethodName) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, API_URL_FOR_LOCATION, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub
                // parseResponseData(response); // Parse Data
                listener.onTaskCompleted(response, MethodName);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                listener.onError(error, MethodName);
            }
        });

        MyApplication.getInstance().addToRequestQueue(jsObjRequest);

    }

    /***
     * Get the Packages for Subscription
     *
     * @param listener
     * @param MethodName
     */
    public void sendQuestionnaireData(OnTaskCompleted listener, String MethodName, String... params) {
        try {

            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);

            //Added by Iziss to send data to server
            mChildData.put("StudentID", params[0]);
            mChildData.put("StudentEmailID", params[1]);
            mChildData.put("VPUse", params[2]);
            mChildData.put("VPExam", params[3]);
            mChildData.put("VPYear", params[4]);

            if (params != null) {
                if (params.length > 5) {
                    mChildData.put("IsUpdate", params[5]);
                }
            }

            mParentData.put("body", mChildData);
            DebugLog.d("", "Questionnaire Request>>" + mParentData.toString());

            //Toast.makeText(mContext, mParentData.toString(), Toast.LENGTH_LONG).show();

            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * Get the Packages for Subscription
     *
     * @param listener
     * @param MethodName
     */
    public void isEmailAvailableOnMixpanel(OnTaskCompleted listener, String MethodName, String... params) {
        try {

            mParentData = new JSONObject();
            mChildData = new JSONObject();

            mParentData.put("method", MethodName);

            //Added by Iziss to send data to server
            mChildData.put("StudentID", params[0]);
            mChildData.put("DeviceID", params[1]);
            mChildData.put("MixpanelDistinctID", params[2]);
            mChildData.put("MixpanelPeopleDistinctID", params[3]);

            mParentData.put("body", mChildData);
            DebugLog.d("", "Mixpanel Request>>" + mParentData.toString());

            //Toast.makeText(mContext, mParentData.toString(), Toast.LENGTH_LONG).show();

            sendRequestWithPostData(DOMAIN_NAME, mParentData, listener, MethodName); // parameters

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


}
