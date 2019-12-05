package com.visualphysics;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import Database.DataBase;
import Model.AppMenu;
import Model.Category;
import Model.Chapters;
import Model.LoginUser;
import Model.Videos;
import Utils.AppConstansts;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.LicenseUtil;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;

public class DeepLinkingActivity extends AppCompatActivity implements OnTaskCompleted.CallBackListener {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash_screen_activity);

        //Check if the app is not install from store then dont allow to excess app
        /*if(!AppUtil.isDownloadedFromPlayStore(getApplicationContext())){
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.msg_app_not_install_from_store),
                    Toast.LENGTH_LONG).show();
            finish();
        }
        else*/

        mDeclaration();

    }

    /***
     * Initialize all resource of screen
     */
    private void mDeclaration() {
        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(DeepLinkingActivity.this);
        mProgressDialog = new ProgressDialog(DeepLinkingActivity.this);
        mProgressDialog.setCancelable(false);
        db = new DataBase(getApplicationContext());
        //mAppUtils.getKeyHash();
        mSharedPref = new SharedPrefrences(getApplicationContext());
        userLogin = mSharedPref.getLoginUser();

        //Check the last open app UTC date time with current UTC date TIme
        if (checkLastOpenAppDateTime()) {

            //check if data is exist or not
            //if (db.isCategoryAvailable() && db.isChapterAvailable() && db.isChapterVideoAvailable()) {
            if (db.isChapterAvailable() && db.isChapterVideoAvailable()) {
                if (!mAppUtils.getConnectionState()) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {

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
                    }, 3000L);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            checkUpateApp();
                        }
                    }, 3000L);

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
                            checkUpateApp();//getLatestUpdate();
                        }
                    }, 3000L);

                } else {
                    //Get the APplication Menu
                    //getAppMenu();

                    //Get All Chapter and Videos
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
            }, 3000L);
        } else
            getCategoryList();*/
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {

        if (Method.equals(mApiCall.GetCategories)) {
            parseResponseForCategoryData(result);
        } else if (Method.equals(mApiCall.AppMenu))
            parseResponseForAppMenuData(result);
        else if (Method.equals(mApiCall.CheckUpdates))
            parseResponseForLatestUpdateData(result);
        else if (Method.equals(mApiCall.GetAllChapterAndVideos)) {

            parseResponseForAllChaptersData(result);
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
            mProgressDialog.show();

            String UserID = "";
            if (mSharedPref.getLoginUser() != null)
                UserID = mSharedPref.getLoginUser().getStudentID();

            mApiCall.getLatestUpdate(UserID,
                    mAppUtils.getDeviceID(), mSharedPref.getPreferences(mSharedPref.LAST_SYNC_DATE, ""), strUTCDateTime,
                    new OnTaskCompleted(this), mApiCall.CheckUpdates);

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
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetAllChapterAndVideos);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
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

        } else
            getCategoryList();
    }

    /***
     * Parse the response of Check Update
     *
     * @param response
     */
    private void parseResponseForLatestUpdateData(JSONObject response) {
        int CurrentAppVersion = -1;
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.CheckUpdates);
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
                    LicenseUtil License = new LicenseUtil();
                    License.removeLicenseOnDelinkDevice();
                } catch (Exception e) {
                    e.printStackTrace();
                    ErrorLog.SendErrorReport(e);
                }


                if (mSharedPref.getLoginUser() != null) {
                    DelinkMsg = response.getJSONObject(mApiCall.CheckUpdates).getString("Message");
                    isDeviceDelink = true;
                }
            } else if (ErrorCode == 1) {

                //Set Config data
                try {
                    //Video base URL Detail
                    mSharedPref.setPreferences(mSharedPref.STREAM_URL, mJsonObj.getJSONObject("config").getString("OnlineURL"));
                    mSharedPref.setPreferences(mSharedPref.DOWNLOAD_URL, mJsonObj.getJSONObject("config").getString("OfflineURL"));
                    mSharedPref.setPreferences(mSharedPref.SUPPORT_EMAIL, mJsonObj.getJSONObject("config").getString("SupportEmail"));
                    mSharedPref.setPreferences(mSharedPref.APP_VERSION, mJsonObj.getJSONObject("config").getString("AppVersion"));
                } catch (Exception e) {
                    e.printStackTrace();
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


        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {
            CheckDataAvailable();
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
                        Intent intent = new Intent(DeepLinkingActivity.this, ReferalCodeScreenActivity.class);
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
                AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(DeepLinkingActivity.this,
                        R.style.AppCompatAlertDialogStyle);
                myAlertDialog.setTitle(R.string.app_name);
                myAlertDialog.setMessage("Please connect to internet for updating database.");
                myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // do something when the OK button is clicked
                        finish();
                    }
                });

                myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) { // do something

                        finish();
                    }
                });

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
                        Intent intent = new Intent(DeepLinkingActivity.this, ReferalCodeScreenActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
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
                    AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(DeepLinkingActivity.this,
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
                Toast.makeText(DeepLinkingActivity.this, R.string.msg_install_latest_version, Toast.LENGTH_SHORT).show();
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
}
