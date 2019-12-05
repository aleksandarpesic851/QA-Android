package com.visualphysics;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.Runtime;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import Database.DataBase;
import Model.AppMenu;
import Model.Category;
import Model.Chapters;
import Model.LoginUser;
import Model.SearchKeywords;
import Model.Videos;
import Utils.AppConstansts;
import Utils.AppUtil;
import Utils.ApplicationConfiguration;
import Utils.ErrorLog;
import Utils.LicenseUtil;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import firebase.FirebaseClass;
import mixpanel.MixPanelClass;
import zoho.ZohoUtils;

public class SplashScreenActivity extends AppCompatActivity implements OnTaskCompleted.CallBackListener {

    private ArrayList<Category> mCategoryArrayList;
    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private DataBase db;
    private SharedPrefrences mSharedPref;

    private ArrayList<Chapters> mChapterArrayList;
    private ArrayList<Videos> mVideoArrayList;

    private LoginUser userLogin;

    private boolean isDeviceDelink = false; //If device is delink this status will be true
    private String DelinkMsg = "";
    public static Uri referralCodeDeepLink = null;

    public final long SPLASH_DELAY = 1000; // 1 Second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash_screen_activity);

        //Check if the app is not install from store then dont allow to excess app
        /*if (!AppUtil.isDownloadedFromPlayStore(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.msg_app_not_install_from_store),
                    Toast.LENGTH_LONG).show();
            finish();
        } else
        {*/
        //This object will accept key and its value to send with MixPanel data
        HashMap<String, String> hashMap = new HashMap<>();
        //Key and their values which will be send to Mix Panel
        hashMap.put(MixPanelClass.MPE_APP_OPEN, AppUtil.getCurrentDateTime(this));

        //This will enable the version of TLS for kitkat and pre kitkat devices
        enableTLSV2();

        //Initialize Mixpanel class and sent it
        MixPanelClass mixPanelClass = new MixPanelClass(this);
        mixPanelClass.sendData(MixPanelClass.MPE_APP_OPEN, hashMap);

        setUpDeepLinking();

        new FirebaseClass(SplashScreenActivity.this).sendCustomAnalyticsData(FirebaseAnalytics.Event.APP_OPEN, "App Opened");

        mDeclaration();
        //}

        //Hide Zoho Chat
        ZohoUtils.hideZohoChat();

        //This will send firebase device token to ZOHO
        ZohoUtils.setPushToken();

    }

    /***
     * Initialize all resource of screen
     */
    private void mDeclaration() {
        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(SplashScreenActivity.this);
        mProgressDialog = new ProgressDialog(SplashScreenActivity.this);
        mProgressDialog.setCancelable(false);
        db = new DataBase(getApplicationContext());
        //mAppUtils.getKeyHash();
        mSharedPref = new SharedPrefrences(getApplicationContext());
        userLogin = mSharedPref.getLoginUser();
        ApplicationConfiguration.isRATE_POPUP_SHOWN = false; // Change to false on start of the app

        getSearchKeywords();

        //Check the last open app UTC date time with current UTC date TIme
        if (checkLastOpenAppDateTime()) {

            //check if data is exist or not
            //if (db.isCategoryAvailable() && db.isChapterAvailable() && db.isChapterVideoAvailable()) {
            if (db.isChapterAvailable() && db.isChapterVideoAvailable()) {
                if (!mAppUtils.getConnectionState()) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {

                            sendCustomLog("From Handler 01");

                            //check Update app validation
                            checkUpateApp();

                            /*if (userLogin == null) {
                                startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                                finish();
                            } else {
                                // If user register successfully but not verify the OTP then move to OTP screen
                                if(userLogin.getIsOTPChecked().equals("0")){
                                    Intent intent = new Intent(getApplicationContext(), OTPScreen.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Intent intent = new Intent(getApplicationContext(), NavigationScreen.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }*/

                        }
                    }, SPLASH_DELAY);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            sendCustomLog("From Handler 02");

                            checkUpateApp();
                        }
                    }, SPLASH_DELAY);

                    /*if (!mSharedPref.getPreferences(mSharedPref.LAST_SYNC_DATE, "").equals("")) {
                        getLatestUpdate();
                    } else {
                        //Get the APplication Menu
                        //getAppMenu();
                        getLatestUpdate();
                    }*/
                }
            } else {
                if (!mSharedPref.getPreferences(mSharedPref.LAST_SYNC_DATE, "").equals("")) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {

                            sendCustomLog("From Handler 03");
                            checkUpateApp();//getLatestUpdate();
                        }
                    }, SPLASH_DELAY);

                } else {
                    //Get the APplication Menu
                    //getAppMenu();

                    //Get All Chapter and Videos

                    sendCustomLog("Get All Chapters 01");
                    getAllChapters();
                }
            }

        }
       /* //Check is Category already available in database then don't call category webservice
        if (db.isCategoryAvailable() && db.isChapterAvailable()) {
            new Handler().postDelayed(new Runnable() {
                public void run() {

                    if (userLogin == null) {
                        startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                        finish();
                    }
                    else{
                        Intent intent = new Intent(getApplicationContext(), NavigationScreen.class);
                        startActivity(intent);
                        finish();
                    }

                }
            }, SPLASH_DELAY);
        } else
            getCategoryList();*/
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        if (Method.equals(mApiCall.GetCategories)) {
            sendCustomLog("Task Complete Get Categories" + result.toString());
            parseResponseForCategoryData(result);
        } else if (Method.equals(mApiCall.AppMenu)) {
            sendCustomLog("Task Complete App Menu" + result.toString());
            parseResponseForAppMenuData(result);
        } else if (Method.equals(mApiCall.CheckUpdates)) {
            sendCustomLog("Task Complete Check Update" + result.toString());
            parseResponseForLatestUpdateData(result);
        } else if (Method.equals(mApiCall.GetAllChapterAndVideos)) {
            sendCustomLog("Task Complete Get all chapter and videos" + result.toString());
            parseResponseForAllChaptersData(result);
        } else if (Method.equals(mApiCall.GetSearchKeyword)) {
            sendCustomLog("Task Complete Get Search Keywords" + result.toString());
            parseResponseForSearchKeyword(result);
        } else if (Method.equals(mApiCall.PostSearchKeyword)) {
            sendCustomLog("Task Complete Post Search Keywords" + result.toString());
            parseResponseForPostSearchKeyword(result);
        }

    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.AppMenu) || Method.equals(mApiCall.CheckUpdates)) {
            CheckDataAvailable();
            sendCustomLog("Error " + Method);

        } else
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
    }

    /***
     * Get the Category HomeScreenFragment
     */
    private void getCategoryList() {
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
            mApiCall.getCategories(mAppUtils.getDeviceID(),
                    new OnTaskCompleted(this), mApiCall.GetCategories);

            sendCustomLog("API Call " + mApiCall.GetCategories);

        }
    }

    /***
     * Get All Chapters of All Cateogory
     */
    private void getAllChapters() {
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
            mApiCall.getAllChaptersAndAllVideos(mAppUtils.getDeviceID(),
                    new OnTaskCompleted(this), mApiCall.GetAllChapterAndVideos);

            sendCustomLog("API Call " + mApiCall.GetAllChapterAndVideos);

        }
    }

    /***
     * Get Search keywords for global search
     */
    private void getSearchKeywords() {
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else {
            List<SearchKeywords> searchKeywords = db.checkAndGetSearchKeywordsToUpload();
            if (!searchKeywords.isEmpty()) {
                JSONArray jsonArray = new JSONArray();
                try {
                    for (SearchKeywords item : searchKeywords) {
                        JSONObject searchKeyword = new JSONObject();
                        searchKeyword.put("Keyword", item.getSearchKeyword());
                        searchKeyword.put("HitCount", item.getHitCount());
                        searchKeyword.put("Status", item.getStatus());
                        jsonArray.put(searchKeyword);
                    }
                    mApiCall.postSearchKeywords(mAppUtils.getDeviceID(),
                            new OnTaskCompleted(this), mApiCall.PostSearchKeyword, jsonArray);
                } catch (Exception ex) {
                    sendCustomLog("Search data construct fail" + mApiCall.GetSearchKeyword);
                }
            }

            mApiCall.getSearchKeywords(mAppUtils.getDeviceID(),
                    new OnTaskCompleted(this), mApiCall.GetSearchKeyword);

            sendCustomLog("API Call " + mApiCall.GetSearchKeyword);

        }
    }

    /***
     * Get the application that not to be display in left menu
     */
    private void getAppMenu() {
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
            mApiCall.getAppMenu(mAppUtils.getDeviceID(), new OnTaskCompleted(this), mApiCall.AppMenu);

            sendCustomLog("API Call " + mApiCall.AppMenu);
        }
    }

    /***
     * Get the latest update if any avilable
     */
    private void getLatestUpdate() {
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else {
            String strUTCDateTime = "";
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
                Date UTCDateTime = format.parse(mAppUtils.getUTCTime());

                format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                strUTCDateTime = format.format(UTCDateTime);
            } catch (Exception e) {
                e.printStackTrace();
                ErrorLog.SendErrorReport(e);
            }

            mProgressDialog.setMessage("Please wait...");
            if (!this.isFinishing())
                mProgressDialog.show();

            String UserID = "";
            if (mSharedPref.getLoginUser() != null)
                UserID = mSharedPref.getLoginUser().getStudentID();

            mApiCall.getLatestUpdate(UserID,
                    mAppUtils.getDeviceID(), mSharedPref.getPreferences(mSharedPref.LAST_SYNC_DATE, ""), strUTCDateTime,
                    new OnTaskCompleted(this), mApiCall.CheckUpdates);

            sendCustomLog("API Call " + mApiCall.CheckUpdates);

        }
    }

    /***
     * Parse the response of CategoryList
     *
     * @param response
     */
    private void parseResponseForCategoryData(JSONObject response) {
        try {
            Log.d("", "Response ----- " + response.toString());
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetCategories);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1) {
                dismissDialog();
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            } else if (ErrorCode == 2) {
                dismissDialog();
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            } else if (ErrorCode == 0) {

                sendCustomLog("Category Data " + response.toString());

                if (db.isCategoryAvailable())
                    db.deleteAllChapterRecords();

                /*Gson gson = new GsonBuilder().serializeNulls().create();
                JsonParser jparsor = new JsonParser();
                JsonArray jaArray = jparsor.parse(mJsonObj.getString("data")).getAsJsonArray();
                Category[] CategoryArray = gson.fromJson(jaArray, Category[].class);
                mCategoryArrayList = new ArrayList<Category>(Arrays.asList(CategoryArray));*/

                JSONArray jsonArray = new JSONArray(mJsonObj.getString("data"));
                mCategoryArrayList = Category.fromJson(jsonArray);

                //Insert the Category Data into Database
                if (db.doAddCategory(mCategoryArrayList)) {
                    getAllChapters();
                } else
                    mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));

            }
        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }


    /***
     * Parse the response of Chapters
     *
     * @param response
     */
    private void parseResponseForAllChaptersData(JSONObject response) {
        try {
            JSONObject mJsonObj;
            //If there is encryption error
            if (response.has("encryption_error")) {
                mJsonObj = response.getJSONObject("encryption_error");
            } else
                mJsonObj = response.getJSONObject(mApiCall.GetAllChapterAndVideos);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2) {
                //If there is encrypt error then throw the user to play store to install the new app
                if (mJsonObj.has("cryp_Error") && mJsonObj.getInt("cryp_Error") == 6) {
                    Toast.makeText(this, mJsonObj.getString("Message"), Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                            finish();
                        }
                    }, 2000L);
                } else {
                    mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                            mJsonObj.getString("Message"));
                }
            } else if (ErrorCode == 0) {
                /*Gson gson = new GsonBuilder().serializeNulls().create();
                JsonParser jparsor = new JsonParser();*/

                mJsonObj = mJsonObj.getJSONObject("data");

                //Get Chapters
                /*JsonArray jaChapterArray = jparsor.parse(mJsonObj.getString("chapter")).getAsJsonArray();
                Chapters[] ChapterArray = gson.fromJson(jaChapterArray, Chapters[].class);
                mChapterArrayList = new ArrayList<Chapters>(Arrays.asList(ChapterArray));*/
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("chapter"));
                mChapterArrayList = Chapters.fromJson(jsonArray);

                //Videos
                /*JsonArray jaVideoArray = jparsor.parse(mJsonObj.getString("video")).getAsJsonArray();
                Videos[] VideoArray = gson.fromJson(jaVideoArray, Videos[].class);
                mVideoArrayList = new ArrayList<Videos>(Arrays.asList(VideoArray));*/
                jsonArray = new JSONArray(mJsonObj.getString("video"));
                mVideoArrayList = Videos.fromJson(jsonArray);

                //Insert the Category Data into Database
                new AsyncTaskRunner().execute();
                /*if (db.doAddChapters(mChapterArrayList) && db.doAddChapterVideos(mVideoArrayList)) {

                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
                    Date UTCDateTime = null;
                    try {
                        UTCDateTime = format.parse(mAppUtils.getUTCTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //Log.d("","UTC TIme newDate ----------------------- "+newDate.toString());

                    format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String strUTCDateTime = format.format(UTCDateTime);
                    mSharedPref.setPreferences(mSharedPref.LAST_SYNC_DATE, strUTCDateTime);

                    // Log.d("","UTC TIme ----------------------- "+date);}

                    if (userLogin == null) {
                        startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                        finish();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), NavigationScreen.class);
                        startActivity(intent);
                        finish();
                    }


                } else
                    mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));
                */


            }
        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }


    /***
     * Parse the response of App Menu
     *
     * @param response
     */
    private void parseResponseForAppMenuData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AppMenu);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1) {
                dismissDialog();
                /*mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));*/
            } else if (ErrorCode == 2) {
                dismissDialog();
                /*mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));*/
            } else if (ErrorCode == 0) {
                /*Gson gson = new GsonBuilder().serializeNulls().create();
                JsonParser jparsor = new JsonParser();
                JsonArray jaArray = jparsor.parse(mJsonObj.getString("data")).getAsJsonArray();
                AppMenu[] AppMenuArray = gson.fromJson(jaArray, AppMenu[].class);
                ArrayList mAppMenuArrayList = new ArrayList<AppMenu>(Arrays.asList(AppMenuArray));*/
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("data"));
                ArrayList mAppMenuArrayList = AppMenu.fromJson(jsonArray);

                //Insert the Category Data into Database
                if (db.doAddAppMenu(mAppMenuArrayList)) {
                    //Check is Category already available in database then don't call category webservice
                    //CheckDataAvailable();

                } else
                    mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));

            }
        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();
            /*mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));*/
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {
            CheckDataAvailable();
        }

    }

    /**
     *
     */
    private void CheckDataAvailable() {
        //if (db.isCategoryAvailable() && db.isChapterAvailable() && db.isChapterVideoAvailable()) {
        if (db.isChapterAvailable() && db.isChapterVideoAvailable()) {
            dismissDialog();
            /*if (userLogin == null) {
                startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                finish();
            } else {
                // If user register successfully but not verify the OTP then move to OTP screen
                if(userLogin.getIsOTPChecked().equals("0")){
                    Intent intent = new Intent(getApplicationContext(), OTPScreen.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), NavigationScreen.class);
                    startActivity(intent);
                    finish();
                }
            }*/
            moveToNextScreen();

            sendCustomLog("Move to next screen ");

        } else {
            getCategoryList();

            sendCustomLog("Get List");
        }
    }

    /**
     *
     */
    private void checkSearchDataAvailable() {
        //if (db.isCategoryAvailable() && db.isChapterAvailable() && db.isChapterVideoAvailable()) {
        if (db.isSearchKeywordDataAvailable()) {
            dismissDialog();
            sendCustomLog("Move to next screen ");
        } else {
//            getCategoryList();

            sendCustomLog("Get List");
        }
    }

    /***
     * Parse the response of Check Update
     *
     * @param response
     */
    private void parseResponseForLatestUpdateData(JSONObject response) {
        int CurrentAppVersion = -1;
        boolean isEncryptionError = false;
        try {
            JSONObject mJsonObj;

            if (response.has("encryption_error")) {
                mJsonObj = response.getJSONObject("encryption_error");
                if (mJsonObj.getInt("Error") == 2) {
                    //If there is encrypt error then throw the user to play store to install the new app
                    if (mJsonObj.has("cryp_Error") && mJsonObj.getInt("cryp_Error") == 6) {
                        isEncryptionError = true;
                        Toast.makeText(this, mJsonObj.getString("Message"), Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                                finish();
                            }
                        }, 2000L);
                    } else {
                        mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                                mJsonObj.getString("Message"));
                    }
                } else {
                    mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                            mJsonObj.getString("Message"));
                }

            } else {
                mJsonObj = response.getJSONObject(mApiCall.CheckUpdates);

                int ErrorCode = mJsonObj.getInt("Error");

                /*Gson gson = new GsonBuilder().serializeNulls().create();
                JsonParser jparsor = new JsonParser();*/

                mJsonObj = mJsonObj.getJSONObject("data");

                //Set the default count for the APP Open
                setDefaultCountForAppOpen();

                //Add Last Syn UTC date time
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
                Date UTCDateTime = format.parse(mAppUtils.getUTCTime());

                format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String strUTCDateTime = format.format(UTCDateTime);
                mSharedPref.setPreferences(mSharedPref.LAST_SYNC_DATE, strUTCDateTime);

                //When device is delink
                if (ErrorCode == 5) {
                    try {
                        Runtime.initialize(getDir("wasabi", MODE_PRIVATE).getAbsolutePath());
                        LicenseUtil License = new LicenseUtil();
                        License.removeLicenseOnDelinkDevice();

                        sendCustomLog("Device Removed");

                    } catch (ErrorCodeException e) {
                        ErrorLog.SendErrorReport(e);
                    } catch (Exception e) {
                        ErrorLog.SendErrorReport(e);
                    }


                    if (mSharedPref.getLoginUser() != null) {
                        DelinkMsg = response.getJSONObject(mApiCall.CheckUpdates).getString("Message");
                        isDeviceDelink = true;
                    }
                } else if (ErrorCode == 1) {

                    //Set Config data
                    try {

                        sendCustomLog("Save URL Start");

                        //Video base URL Detail
                        mSharedPref.setPreferences(mSharedPref.STREAM_URL, mJsonObj.getJSONObject("config").getString("OnlineURL"));
                        mSharedPref.setPreferences(mSharedPref.DOWNLOAD_URL, mJsonObj.getJSONObject("config").getString("OfflineURL"));
                        mSharedPref.setPreferences(mSharedPref.SUPPORT_EMAIL, mJsonObj.getJSONObject("config").getString("SupportEmail"));
                        mSharedPref.setPreferences(mSharedPref.APP_VERSION, mJsonObj.getJSONObject("config").getString("AppVersion"));

                        sendCustomLog("Save URL complete");

                    } catch (Exception e) {
                        e.printStackTrace();
                        ErrorLog.SendErrorReport(e);
                    }
                } else if (ErrorCode == 2) {
                    //If there is encrypt error then throw the user to play store to install the new app
                    if (mJsonObj.has("cryp_Error") && mJsonObj.getInt("cryp_Error") == 6) {
                        Toast.makeText(this, mJsonObj.getString("Message"), Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                                finish();
                            }
                        }, 2000L);
                    } else {
                        mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                                mJsonObj.getString("Message"));
                    }
                } else if (ErrorCode == 0) {

                    //Get Chapters
                    /*JsonArray jaChapterArray = jparsor.parse(mJsonObj.getString("chapter")).getAsJsonArray();
                    Chapters[] ChapterArray = gson.fromJson(jaChapterArray, Chapters[].class);
                    ArrayList mChapterArrayList = new ArrayList<Chapters>(Arrays.asList(ChapterArray));*/
                    JSONArray jsonArray = new JSONArray(mJsonObj.getString("chapter"));
                    ArrayList mChapterArrayList = Chapters.fromJson(jsonArray);
                    if (mChapterArrayList != null && mChapterArrayList.size() > 0)
                        db.doAddChapters(mChapterArrayList);


                    //Videos
                    /*JsonArray jaVideoArray = jparsor.parse(mJsonObj.getString("video")).getAsJsonArray();
                    Videos[] VideoArray = gson.fromJson(jaVideoArray, Videos[].class);
                    ArrayList mVideoArrayList = new ArrayList<Videos>(Arrays.asList(VideoArray));*/
                    jsonArray = new JSONArray(mJsonObj.getString("video"));
                    ArrayList mVideoArrayList = Videos.fromJson(jsonArray);
                    if (mVideoArrayList != null && mVideoArrayList.size() > 0)
                        db.doAddChapterVideos(mVideoArrayList);

                    try {
                        //Video base URL Detail
                        mSharedPref.setPreferences(mSharedPref.STREAM_URL, mJsonObj.getJSONObject("config").getString("OnlineURL"));
                        mSharedPref.setPreferences(mSharedPref.DOWNLOAD_URL, mJsonObj.getJSONObject("config").getString("OfflineURL"));
                        mSharedPref.setPreferences(mSharedPref.SUPPORT_EMAIL, mJsonObj.getJSONObject("config").getString("SupportEmail"));
                        mSharedPref.setPreferences(mSharedPref.APP_VERSION, mJsonObj.getJSONObject("config").getString("AppVersion"));

                    } catch (Exception e) {
                        e.printStackTrace();
                        ErrorLog.SendErrorReport(e);
                    }
                }

                //Get App Menu
                /*JsonArray jaArray = jparsor.parse(mJsonObj.getString("appmenu")).getAsJsonArray();
                AppMenu[] AppMenuArray = gson.fromJson(jaArray, AppMenu[].class);
                ArrayList mAppMenuArrayList = new ArrayList<AppMenu>(Arrays.asList(AppMenuArray));*/
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("appmenu"));
                ArrayList mAppMenuArrayList = AppMenu.fromJson(jsonArray);

                dismissDialog();

                //Insert the Category Data into Database
                if (db.doAddAppMenu(mAppMenuArrayList)) {
                   /* new Handler().postDelayed(new Runnable() {
                        public void run() {

                            if (userLogin == null) {
                                startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                                finish();
                            } else {
                                Intent intent = new Intent(getApplicationContext(), NavigationScreen.class);
                                startActivity(intent);
                                finish();
                            }

                        }
                    }, 1000L);*/


                } else
                    mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));
            }

        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {
            if (!isEncryptionError)
                CheckDataAvailable();
        }
    }

    /***
     * Parse the response of SearchKeyword
     *
     * @param response
     */
    private void parseResponseForSearchKeyword(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetSearchKeyword);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1) {
                dismissDialog();
            } else if (ErrorCode == 2) {
                dismissDialog();
            } else if (ErrorCode == 0) {
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("data"));
                ArrayList mSearchKeywordsArrayList = SearchKeywords.fromJson(jsonArray);

                if (db.doAddSearchKeywords(mSearchKeywordsArrayList)) {

                } else
                    mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));

            }
        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();
            /*mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));*/
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {
            checkSearchDataAvailable();
        }

    }

    /***
     * Parse the response of SearchKeyword
     *
     * @param response
     */
    private void parseResponseForPostSearchKeyword(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.PostSearchKeyword);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1) {
                dismissDialog();
            } else if (ErrorCode == 2) {
                dismissDialog();
            } else if (ErrorCode == 0) {
                String  message = mJsonObj.getString("Message");


            }
        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();
            /*mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));*/
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {
            checkSearchDataAvailable();
        }

    }

    /***
     * Dismiss Dialog
     */
    private void dismissDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    /***
     * Store Chapters and Videos
     */
    private class AsyncTaskRunner extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            if (db.doAddChapters(mChapterArrayList) && db.doAddChapterVideos(mVideoArrayList)) {
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);
            dismissDialog();
            if (status) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
                Date UTCDateTime = null;
                try {
                    UTCDateTime = format.parse(mAppUtils.getUTCTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                    ErrorLog.SaveErrorLog(e);
                    ErrorLog.SendErrorReport(e);
                }
                //Log.d("","UTC TIme newDate ----------------------- "+newDate.toString());

                format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String strUTCDateTime = format.format(UTCDateTime);
                mSharedPref.setPreferences(mSharedPref.LAST_SYNC_DATE, strUTCDateTime);

                // Log.d("","UTC TIme ----------------------- "+date);}

               /* if (userLogin == null) {
                    startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                    finish();
                } else {
                    if(userLogin.getFriendReferralCode().equals("")){
                        Intent intent = new Intent(SplashScreenActivity.this, ReferalCodeScreenActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        // If user register successfully but not verify the OTP then move to OTP screen
                        if (userLogin.getIsOTPChecked().equals("0")) {
                            Intent intent = new Intent(getApplicationContext(), OTPScreen.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), NavigationScreen.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }*/
                moveToNextScreen();


            } else
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
        }
    }


    //Check update app validation
    private void checkUpateApp() {
        checkAppOpenCount();
    }


    //Check the app open count if less count then Add the count less give alert for update the app
    private void checkAppOpenCount() {
        int count = mSharedPref.getPreferences(mSharedPref.APP_OPEN_COUNT, 1);
        checkLastSyncDate();
        if (count >= AppConstansts.APP_OPEN_COUNT || checkLastSyncDate()) {
            if (!mAppUtils.getConnectionState()) {
                AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(SplashScreenActivity.this,
                        R.style.AppCompatAlertDialogStyle);
                myAlertDialog.setTitle(R.string.app_name);
                myAlertDialog.setMessage("Please connect to internet for updating database.");
                myAlertDialog.setPositiveButton("OK", (arg0, arg1) -> {
                    // do something when the OK button is clicked
                    finish();
                });

                myAlertDialog.setNegativeButton("Cancel", (arg0, arg1) -> { // do something

                    finish();
                });

                if (!this.isFinishing())
                    myAlertDialog.show();
            } else {
                getLatestUpdate(); // Update app
            }
        } else {
            addCountForAppOpen();
            moveToNextScreen();
        }
    }

    //When all validation validate then move to next screen
    private void moveToNextScreen() {
        if (userLogin == null) {
            startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
            finish();
        } else {
            if (isDeviceDelink) {
                Toast.makeText(getApplicationContext(), DelinkMsg, Toast.LENGTH_SHORT).show();
                mSharedPref.clearAll();
                Intent intent = new Intent(getApplicationContext(), LoginScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                int CurrentAppVersion = Integer.parseInt(mSharedPref.getPreferences(mSharedPref.APP_VERSION,
                        String.valueOf(mAppUtils.getAppVersionCode())));
                if (checkCurrentVersion(CurrentAppVersion)) {

                    //if(userLogin.getIsOTPChecked().equals("0") && userLogin.getFriendReferralCode().equals("")){
                    if (userLogin.getNewDevice() != null && (userLogin.getNewDevice().equals("1") || userLogin.getNewDevice().equals("0"))) {

                        /*Added by IZISS to change flow of verification*/
                        if (userLogin.getIsOTPChecked().equals("0")) {
                            Intent intent = new Intent(SplashScreenActivity.this, OTPScreen.class);
                            startActivity(intent);
                            finish();
                        } else {

                            //Added to change flow when re-open the application.
                            moveToNextScreenFromSplash();
                        }
                    } else {
                        // If user register successfully but not verify the OTP then move to OTP screen
                        if (userLogin.getIsOTPChecked().equals("0")) {
                            Intent intent = new Intent(getApplicationContext(), OTPScreen.class);
                            startActivity(intent);
                            finish();
                        } else {
                            moveToNextScreenFromSplash();
                        }
                    }
                } else {

                    // If user register successfully but not verify the OTP then move to OTP screen
                    if (userLogin.getIsOTPChecked().equals("0")) {
                        Intent intent = new Intent(getApplicationContext(), OTPScreen.class);
                        startActivity(intent);
                        finish();
                    } else {

                        moveToNextScreenFromSplash();
                    }
                }
            }
        }
    }

    //Check the last sync date , if the  last sync date exceed than the define no of days
    //then update the app
    private boolean checkLastSyncDate() {
        boolean status = false;
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
            Date UTCDateTime = format.parse(mAppUtils.getUTCTime());

            format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String strUTCDateTime = format.format(UTCDateTime);

            if (!mSharedPref.getPreferences(mSharedPref.LAST_SYNC_DATE, "").equals("")) {
                String strLastSyncDate = mSharedPref.getPreferences(mSharedPref.LAST_SYNC_DATE, "");
                Date CurrentDate = null, LastSyncDate = null;
                CurrentDate = format.parse(strUTCDateTime);//catch exception
                LastSyncDate = format.parse(strLastSyncDate);//catch exception

                Calendar sDate = getDatePart(LastSyncDate);
                Calendar eDate = getDatePart(CurrentDate);

                long daysBetween = 0;
                while (sDate.before(eDate)) {
                    sDate.add(Calendar.DAY_OF_MONTH, 1);
                    daysBetween++;
                }
                if (daysBetween >= AppConstansts.APP_LAST_SYNC_DAYS)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;
    }

    public static Calendar getDatePart(Date date) {
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

        return cal;                                  // return the date part
    }

    private void addCountForAppOpen() {
        //Add the count for the APP Open
        mSharedPref.setPreferences(mSharedPref.APP_OPEN_COUNT,
                mSharedPref.getPreferences(mSharedPref.APP_OPEN_COUNT, 1) + 1);
    }

    private void setDefaultCountForAppOpen() {
        //Add the count for the APP Open
        mSharedPref.setPreferences(mSharedPref.APP_OPEN_COUNT,
                1);
    }

    //Check the last open app UTC date time with current UTC date TIme
    private boolean checkLastOpenAppDateTime() {

        boolean status = false;
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
            Date UTCDateTime = format.parse(mAppUtils.getUTCTime());

            format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String strUTCDateTime = format.format(UTCDateTime);

            if (!mSharedPref.getPreferences(mSharedPref.LAST_APP_OPEN_DATE, "").equals("")) {
                String strLastAppOpenDate = mSharedPref.getPreferences(mSharedPref.LAST_APP_OPEN_DATE, "");
                Date CurrentDate = null, LastAppOpenDate = null;
                CurrentDate = format.parse(strUTCDateTime);//catch exception
                LastAppOpenDate = format.parse(strLastAppOpenDate);//catch exception

                Calendar sDate = getDatePart(LastAppOpenDate);
                Calendar eDate = getDatePart(CurrentDate);


                if (CurrentDate.getTime() < LastAppOpenDate.getTime()) {
                    AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(SplashScreenActivity.this,
                            R.style.AppCompatAlertDialogStyle);
                    myAlertDialog.setTitle(R.string.app_name);
                    myAlertDialog.setMessage(R.string.msg_device_time_change);
                    myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            // do something when the OK button is clicked
                            finish();
                        }
                    });

                   /* myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) { // do something

                            finish();
                        }
                    });*/

                    myAlertDialog.show();
                } else {
                    status = true;
                    //Add the Last open date
                    mAppUtils.lastOpenApp(getApplicationContext());
                }
            } else {
                status = true;
                //Add the Last open date
                mAppUtils.lastOpenApp(getApplicationContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

        //  new FirebaseClass(this).sendAnalyticsData(0, "Last App Open", AppUtil.EVENT_LAST_APP_OPEN);

        return status;
    }

    /***
     * Check the latest version of install app with current version on server
     *
     * @param CurrentAppVersion
     */
    private boolean checkCurrentVersion(int CurrentAppVersion) {
        boolean status = true;
        if (CurrentAppVersion > 0) {
            if (mAppUtils.getAppVersionCode() >= CurrentAppVersion) {
                status = true;

            } else {
                status = false;
                Toast.makeText(SplashScreenActivity.this, R.string.msg_install_latest_version, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        return status;
    }

/*    @Override
    protected void onStart() {
        super.onStart();

        // Check if the intent contains an AppInvite and then process the referral information.
        Intent intent = getIntent();
        if (AppInviteReferral.hasReferral(intent)) {
            processReferralIntent(intent);
        }
    }
    // [END deep_link_on_start]

    // [START process_referral_intent]
    private void processReferralIntent(Intent intent) {
        // Extract referral information from the intent
        String invitationId = AppInviteReferral.getInvitationId(intent);
        String deepLink = AppInviteReferral.getDeepLink(intent);

        // Display referral information
        // [START_EXCLUDE]
        Log.d(TAG, "Found Referral: " + invitationId + ":" + deepLink);
        ((TextView) findViewById(R.id.deep_link_text))
                .setText(getString(R.string.deep_link_fmt, deepLink));
        ((TextView) findViewById(R.id.invitation_id_text))
                .setText(getString(R.string.invitation_id_fmt, invitationId));
        // [END_EXCLUDE]
    }
    // [END process_referral_intent]*/


    private void setUpDeepLinking() {

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found) already handled with other code

                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {

                            deepLink = pendingDynamicLinkData.getLink();

                            Log.w("Pending Deep Link", "" + deepLink);

                            referralCodeDeepLink = deepLink;
                        }

                        showToastInBeta(SplashScreenActivity.this, "Referral Link:" + referralCodeDeepLink);

                    }


                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DYNAMIC FAILURE", "getDynamicLink:onFailure", e);

                        showToastInBeta(SplashScreenActivity.this, "Link Fail" + e.toString());

                    }
                });
    }


    /***
     * This will set custom message
     *
     * @param message
     */
    public static void sendCustomLog(String message) {
        //ErrorLog.saveErrorLog(message);
    }

    /**
     * This will forward floe to next screen from splash
     */
    public void moveToNextScreenFromSplash() {

        //Check if the questionnaire screen is appeared once and the app is closed
        MixPanelClass mixPanelClass = new MixPanelClass(this);

        if (mixPanelClass.getPref(MixPanelClass.PREF_IS_QUESTIONNAIRE_COMPLETED, true)) {

            //Show a form to Indian Users only
            Intent navigationIntent = new Intent(this, NavigationScreen.class);
            startActivity(navigationIntent);
            finish();

        } else {

            //Show a form to Indian Users only
            Intent formIntent = new Intent(this, IndianFormActivity.class);
            startActivity(formIntent);
            finish();
        }

    }

    /**
     * This will show a toast message
     *
     * @param message
     */
    public static void showToastInBeta(Context mContext, String message) {
        //Comment me on live version
        // if (message != null)
        //     Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();

    }

    /**
     * This will enable TLSV2 for devices having android version 4.4 and below devices
     * https://stackoverflow.com/questions/24357863/making-sslengine-use-tlsv1-2-on-android-4-4-2/26586324#26586324
     */
    private void enableTLSV2() {
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            SSLEngine engine = sslContext.createSSLEngine();
        } catch (Exception ex) {

        }

    }


/*    @Override
    public void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();

        // Branch init
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {

                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    // ... insert custom logic here ...
                    Log.i("BRANCH SDK", referringParams.toString());

                    *//*Handle referring Params*//*
                    if (referringParams != null) {
                        try {

                            if (referringParams.has("referring_data")) {

                                JSONObject mJsonObject = referringParams.getJSONObject("referring_data");
                                if (mJsonObject != null) {
                                    String channelName = mJsonObject.getString(BranchDeepLink.KEY_CHANNEL);

                                    if (channelName != null) {
                                        if (channelName.equalsIgnoreCase(BranchDeepLink.BR_CHANNEL_NAME)) {
                                        *//*This user is referred by a user, please do code accordingly*//*

     *//*Set a variable to true if a user is referred by someone*//*
                                            BranchDeepLink mBranchDeepLink = new BranchDeepLink(SplashScreenActivity.this);
                                            mBranchDeepLink.setPref(DeepLinkGenerator.PREF_IS_REFERRED_BY_BRANCH, true);

                                        }
                                    }
                                }
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }

                } else {
                    Log.i("BRANCH SDK", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
    }*/

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }


    private void encryptImage() {
        try {

            byte[] keyBytes = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                    0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17}; //Choose a key wisely

            FileInputStream fis;
            CipherInputStream cis;
            FileOutputStream fos;

            String filePath = Environment.getExternalStorageDirectory() + File.separator + "WhatsApp" + File.separator +
                    "Media" + File.separator + "WhatsApp Images" + File.separator + "20190218_122959.jpg";

            String filePathAfterEncrypt = Environment.getExternalStorageDirectory() + File.separator + "WhatsApp" + File.separator +
                    "Media" + File.separator + "WhatsApp Images" + File.separator + "20190218_122959_Enc.enc";

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            fis = new FileInputStream(filePath);
            cis = new CipherInputStream(fis, cipher);
            fos = new FileOutputStream(filePathAfterEncrypt);

            byte[] b = new byte[1024];

            int i = cis.read(b);
            while (i != -1) {
                fos.write(b, 0, i);
                i = cis.read(b);
            }
            fos.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void decryptImage() {
        try {

            byte[] keyBytes = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                    0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17}; //Choose a key wisely

            FileInputStream fis;
            OutputStream fos;
            //FileOutputStream fos;

            String filePathAfterEncrypt = Environment.getExternalStorageDirectory() + File.separator + "WhatsApp" + File.separator +
                    "Media" + File.separator + "WhatsApp Images" + File.separator + "20190218_122959_Enc.enc";

            String filePathActual = Environment.getExternalStorageDirectory() + File.separator + "WhatsApp" + File.separator +
                    "Media" + File.separator + "WhatsApp Images" + File.separator + "20190218_122959_actual.jpg";

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, key);

            fis = new FileInputStream(filePathAfterEncrypt);
            fos = new FileOutputStream(filePathActual);

            fos = new CipherOutputStream(fos, cipher);

            int count = 0;
            byte[] buffer = new byte[1024];
            while ((count = fis.read(buffer)) >= 0) {
                fos.write(buffer, 0, count);
            }

            fos.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
/*@TODO: Touse it in future*/
/*class EncryptDecrypt {

    private final static int DEFAULT_READ_WRITE_BLOCK_BUFFER_SIZE = 1024;
    private final static String ENCRYPTION_TYPE = "AES";
    private final static String ALGORITHM = "AES/CBC/PKCS5Padding";
    private final static String KEY = "SP@#$JUIL12PK&5";//Choose a key wisely

    *//**
 * To encrypt a image file
 *
 * @param inputFilePath
 * @param outputFilePathForEncryptedFile
 *//*
    public static void encrypt(String inputFilePath, String outputFilePathForEncryptedFile) {
        try {

            FileInputStream fis;
            CipherInputStream cis;
            FileOutputStream fos;

            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ENCRYPTION_TYPE);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            fis = new FileInputStream(inputFilePath);
            cis = new CipherInputStream(fis, cipher);
            fos = new FileOutputStream(outputFilePathForEncryptedFile);

            byte[] buffer = new byte[DEFAULT_READ_WRITE_BLOCK_BUFFER_SIZE];
            int count = 0;

            while ((count = cis.read(buffer)) >= 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    *//***
 * To decrypted a encrypted file
 *
 * @param inputFilePathOfEncryptedFile
 * @param outputFilePathForOriginalFile
 *//*
    public static void decrypt(String inputFilePathOfEncryptedFile, String outputFilePathForOriginalFile) {

        try {

            FileInputStream fis;
            OutputStream fos;

            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ENCRYPTION_TYPE);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            fis = new FileInputStream(inputFilePathOfEncryptedFile);
            fos = new FileOutputStream(outputFilePathForOriginalFile);

            fos = new CipherOutputStream(fos, cipher);

            byte[] buffer = new byte[DEFAULT_READ_WRITE_BLOCK_BUFFER_SIZE];
            int count = 0;

            while ((count = fis.read(buffer)) >= 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}*/
