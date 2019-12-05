package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ApplicationConfiguration;
import Utils.DebugLog;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import firebase.FirebaseClass;
import mixpanel.MixPanelClass;
import zoho.ZohoUtils;

/**
 * Created by iziss on 17/11/17.
 */
public class LoginScreenActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, OnTaskCompleted.CallBackListener {

    //Signing Options
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;
    private int RC_SIGN_IN = 10001;

    public static CallbackManager callbackManager;
    public boolean LoginWithFacebook = false;

    private String DeviceModelNumber, DeviceManufacturerName, DeviceOSVersion, DeviceOS = "Android";
    private String strEmail, strPassword, RegisterType, RegisterID, strUserName, strGender = "Male";
    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private SharedPrefrences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_new_login_screen);
        mDeclaration();
        getDeviceInfo();

        //Hide Zoho Chat
        ZohoUtils.hideZohoChat();

        //Code for MixPanel by IZISS
        //MixPanelClass mixPanelClass = new MixPanelClass(this);
        //mixPanelClass.sendData("Login Screen Activity");
    }

    void mDeclaration() {
        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getApplicationContext());
        mProgressDialog = new ProgressDialog(LoginScreenActivity.this);
        mProgressDialog.setCancelable(false);
        mSharedPref = new SharedPrefrences(getApplicationContext());


        TextView mLoginButton = (TextView) findViewById(R.id.btnLogin_LoginScreen);
        Button mImgBtnGPlus = (Button) findViewById(R.id.imgBtnGooglePlusLoginScreen);
        Button mImgBtnFacebook = (Button) findViewById(R.id.imgBtnFacebookLoginScreen);

        mLoginButton.setOnClickListener(this);
        mImgBtnGPlus.setOnClickListener(this);
        mImgBtnFacebook.setOnClickListener(this);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Check is user try to login with guest user
        checkIsSecondaryDevice();

    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        LoginWithFacebook = false;
        switch (v.getId()) {
            case R.id.btnLogin_LoginScreen:

                intent = new Intent(LoginScreenActivity.this, NormalLoginActivity.class);
                startActivity(intent);

                break;

            case R.id.btnForgotPasswordLoginScreen:
                // intent = new Intent(LoginScreenActivity.this, ForgotPasswordScreen.class);
                // startActivity(intent);
                break;


            case R.id.btnSignUpLoginScreen:
                // intent = new Intent(LoginScreenActivity.this, SignUpScreen.class);
                //startActivity(intent);

                break;

            case R.id.imgBtnGooglePlusLoginScreen:

                try {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please try after sometime", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    ErrorLog.SendErrorReport(e);
                }
                break;

            case R.id.imgBtnFacebookLoginScreen:

                LoginWithFacebook = true;
                LoginManager.getInstance().logOut();
                facebookLogin();
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //If signin
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //Calling a new function to handle signin
            handleSignInResult(result);
        } else if (LoginWithFacebook)
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.Login)) {
            parseResponseForLoginData(result);
        } else if (Method.equals(mApiCall.LoginWithSocialMedia)) {
            parseResponseForLoginWithSocialMediaData(result);
        } else if (Method.equals(mApiCall.GetLocation)) {
            parseLocationResponse(result);
        }
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {
        dismissDialog();
        mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                getResources().getString(R.string.Error_Msg_Try_Later));
    }


    /***
     * Check is user try to login with guest user if yes then display the msg for not getting free trial period
     */
    private void checkIsSecondaryDevice() {
        if (!mAppUtils.isOwnerUser()) {
            new AlertDialog.Builder(LoginScreenActivity.this, R.style.AppCompatAlertDialogStyle)
                    .setTitle("")
                    .setMessage(R.string.msg_seconday_device_free_trial)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();
        }
    }

    /***
     * Get the device info
     */
    private void getDeviceInfo() {
        DeviceManufacturerName = Build.MANUFACTURER;
        DeviceModelNumber = Build.MODEL;
        DeviceOSVersion = android.os.Build.VERSION.RELEASE;
    }


    /***
     * Login with Social Media
     */
    private void doLoginWithSocialMedia() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else {
            String UserDeviceType = "Primary";
            if (!mAppUtils.isOwnerUser())
                UserDeviceType = "Secondary";
            String strRefferalCode = "";

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();


            mApiCall.doLoginWithSocialMedia(strUserName, strEmail, "101", DeviceManufacturerName + " " + DeviceModelNumber,
                    DeviceOS, DeviceOSVersion, mAppUtils.getDeviceID(), RegisterType, RegisterID,
                    strRefferalCode, String.valueOf(mAppUtils.getAppVersionCode()), Build.SERIAL, UserDeviceType,
                    new OnTaskCompleted(this), mApiCall.LoginWithSocialMedia);
        }

    }


    /***
     * Parse the response of login data
     *
     * @param response
     */
    private void parseResponseForLoginData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.Login);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                addAppOpenCount(); // Add the count

                Gson gson = new Gson();
                String Data = mJsonObj.getJSONObject("data").toString();
                mSharedPref.setPreferences(mSharedPref.USERDETAIL, Data);

                //Video base URL Detail
                mSharedPref.setPreferences(mSharedPref.STREAM_URL, mJsonObj.getJSONObject("config").getString("OnlineURL"));
                mSharedPref.setPreferences(mSharedPref.DOWNLOAD_URL, mJsonObj.getJSONObject("config").getString("OfflineURL"));
                mSharedPref.setPreferences(mSharedPref.SUPPORT_EMAIL, mJsonObj.getJSONObject("config").getString("SupportEmail"));
                mSharedPref.setPreferences(mSharedPref.APP_VERSION, mJsonObj.getJSONObject("config").getString("AppVersion"));

                mAppUtils.displayToastWithMessage(getApplicationContext(),
                        mJsonObj.getString("Message"));

                LoginUser User = mSharedPref.getLoginUser();
                // If user register successfully but not verify the OTP then move to OTP screen
                if (User.getIsOTPChecked().equals("0")) {
                    Intent intent = new Intent(LoginScreenActivity.this, OTPScreen.class);
                    startActivity(intent);
                    finish();
                } else {

                    //This are older users do do not need to set alias for the users just identify them
                    //Initialize Mixpanel class and sent it
                    MixPanelClass mixPanelClass = new MixPanelClass(this);
                    mixPanelClass.setPref(MixPanelClass.IS_VIDEO_SEEN_AFTER_UPDATE, true);
                    mixPanelClass.sendMixPanelPeopleAttribute(false, true, true);

                    //If user has already played the questionnaire then the value is not blank
                    if (mixPanelClass.isQuestionnaireAnswered()) {
                        navigateToNextScreen();
                    }
                    //We need to check first wether he is an Indian user or not if not then we need to keep the current flow
                    else {
                        checkIndianUser();

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
            ErrorLog.SendSignUpErrorReport(e);
        } finally {

        }
    }

    /***
     * Parse the data of add email data
     *
     * @param response
     */
    private void parseResponseForLoginWithSocialMediaData(JSONObject response) {
        try {

            JSONObject mJsonObj = response.getJSONObject(mApiCall.LoginWithSocialMedia);

            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 3) { //When Email not exist at the Time login with social media


               /* Commented by IZISS on 08 August 2018 to show a banner message if email is not available from Facebook*/
               /* Intent i = new Intent(getApplicationContext(), AddEmailScreenActivity.class);
                i.putExtra("RegisterType", RegisterType);
                i.putExtra("RegisterID", RegisterID);
                i.putExtra("Gender", strGender);
                i.putExtra("Name", strUserName);
                if (strEmail != null && !strEmail.equals("")) {
                    i.putExtra("Email", strEmail);
                }
                startActivity(i);
                finish();*/

                mAppUtils.displaySnackBarWithMessageForTime(findViewById(android.R.id.content),
                        getResources().getString(R.string.msg_direct_login_with_google), 8000);

            } else if (ErrorCode == 0) {

                mAppUtils.displayToastWithMessage(getApplicationContext(),
                        mJsonObj.getString("Message"));

                SharedPrefrences mSharedPref = new SharedPrefrences(getApplicationContext());
                mSharedPref.setPreferences(mSharedPref.USERDETAIL, mJsonObj.getJSONObject("data").toString());

                //Video base URL Detail
                mSharedPref.setPreferences(mSharedPref.STREAM_URL, mJsonObj.getJSONObject("config").getString("OnlineURL"));
                mSharedPref.setPreferences(mSharedPref.DOWNLOAD_URL, mJsonObj.getJSONObject("config").getString("OfflineURL"));
                mSharedPref.setPreferences(mSharedPref.SUPPORT_EMAIL, mJsonObj.getJSONObject("config").getString("SupportEmail"));
                mSharedPref.setPreferences(mSharedPref.APP_VERSION, mJsonObj.getJSONObject("config").getString("AppVersion"));

                //Current flow of the application
                doAfterLogin();

            }
        } catch (Exception e) {

            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendSignUpErrorReport(e);
            ErrorLog.SendErrorReport(e);

        }
    }

    //After the signing we are calling this function
    private void handleSignInResult(GoogleSignInResult result) {
        //If the login succeed
        if (result != null && result.isSuccess()) {

            //Getting google account
            GoogleSignInAccount acct = result.getSignInAccount();
            DebugLog.v("", "Account == " + acct.toString());
            strUserName = acct.getDisplayName();
            strEmail = acct.getEmail();

            DebugLog.v("Google Name", "Name" + acct.getGivenName());

            if (acct.getGivenName() != null) {

                MixPanelClass mixPanelClass = new MixPanelClass(LoginScreenActivity.this);
                mixPanelClass.setPref(MixPanelClass.FIRST_NAME_FROM_SOCIAL_LOGIN, acct.getGivenName());
            }

            RegisterType = ApplicationConfiguration.UserRegType.Google.toString();
            RegisterID = acct.getId();

            addAppOpenCount(); // Add the count

           /* Intent i = new Intent(getApplicationContext(),AddEmailScreenActivity.class);
            i.putExtra("RegisterType",RegisterType);
            i.putExtra("RegisterID",RegisterID);
            i.putExtra("Gender",strGender);
            i.putExtra("Name",strUserName);
            i.putExtra("Email",strEmail);
            startActivity(i);
            finish();*/
            doLoginWithSocialMedia();

        } else {

            //If login fails
            Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
        }
    }


    /**********
     * Facebook
     ******************/
    public void facebookLogin() {

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile", "user_birthday", "user_friends"));

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        // App code
                        DebugLog.v("onSuccess", "onSuccess == " + loginResult.getAccessToken());
                        //Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_LONG).show();
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object,
                                                            GraphResponse response) {
                                        // TODO Auto-generated method stub
                                        try {
                                            //Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
                                            //Log.v("Face", object.toString());
                                            DebugLog.v("Facebook Login>>", object.toString());
                                            strUserName = object.getString("name");
                                            if (object.has("email")) {
                                                strEmail = object.getString("email");
                                            }
                                            if (object.has("gender")) {
                                                if (object.getString("gender").equalsIgnoreCase("male"))
                                                    strGender = "Male";
                                                else if (object.getString("gender").equalsIgnoreCase("female"))
                                                    strGender = "Female";
                                            }

                                            if (object.has("first_name")) {

                                                MixPanelClass mixPanelClass = new MixPanelClass(LoginScreenActivity.this);
                                                mixPanelClass.setPref(MixPanelClass.FIRST_NAME_FROM_SOCIAL_LOGIN, object.getString("first_name"));
                                            }

                                            RegisterType = ApplicationConfiguration.UserRegType.Facebook.toString();
                                            RegisterID = object.getString("id");

                                            addAppOpenCount(); // Add the count

                                           /* Intent i = new Intent(getApplicationContext(),AddEmailScreenActivity.class);
                                            i.putExtra("RegisterType",RegisterType);
                                            i.putExtra("RegisterID",RegisterID);
                                            i.putExtra("Gender",strGender);
                                            i.putExtra("Name",strUserName);
                                            if(strEmail != null || !strEmail.equals("")){
                                                i.putExtra("Email",strEmail);
                                            }
                                            startActivity(i);
                                            finish();*/
                                            doLoginWithSocialMedia();
                                        } catch (Exception e) {
                                            // TODO: handle exception
                                            e.printStackTrace();
                                            //mAppConfig.displayAlert("Please try after some time");
                                            Toast.makeText(getApplicationContext(), "Please try after some time", Toast.LENGTH_LONG).show();
                                            ErrorLog.SaveErrorLog(e);
                                            ErrorLog.SendErrorReport(e);
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,first_name,name,email,gender");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        DebugLog.v("onSuccess", "onCancel == ");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        DebugLog.v("onSuccess", "exception == " + exception);
                        //Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        if (exception.toString().contains("User logged in as different Facebook user")) {
                            LoginManager.getInstance().logOut();
                            Toast.makeText(getApplicationContext(), "Please try after sometime", Toast.LENGTH_LONG).show();

                        }

                    }
                });
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
            ErrorLog.SendSignUpErrorReport(e);
            ErrorLog.SendErrorReport(e);
        }
    }


    //Add the count when the app open
    private void addAppOpenCount() {
        mSharedPref.setPreferences(mSharedPref.APP_OPEN_COUNT, 1);
    }

    /**
     * Detect if the phone's IP is in INDIA COUNTRY to show a form to user other wise move ahead
     *
     * @return true->For Indian Users; false-> Users of other countries (will keep current flow of application)
     */

    private void checkIndianUser() {

        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.show();

        mApiCall.getLocationFromIpAddress(new OnTaskCompleted(this), mApiCall.GetLocation);

    }

    /***
     * Parse the response of location via IP address
     * Detect if the phone's IP is in INDIA COUNTRY to show a form to user other wise move ahead
     *
     * @param response
     */
    private void parseLocationResponse(JSONObject response) {

        // true->For Indian Users; false-> Users of other countries (will keep current flow of application)
        boolean isIndianUser = false;

        try {

            try {

                Log.i("Location Response>>", "" + response);

                if (response.optString(ReferalCodeScreenActivity.KEY_INDIA_COUNTRY_NAME).equalsIgnoreCase(ReferalCodeScreenActivity.INDIA_COUNTRY_NAME) && response.optString(ReferalCodeScreenActivity.KEY_INDIA_COUNTRY_CODE).equalsIgnoreCase(ReferalCodeScreenActivity.INDIA_COUNTRY_CODE)) {
                    isIndianUser = true;
                }

            } catch (Exception e) {

                e.printStackTrace();
                ErrorLog.SendLocationError(e);
            }

            /*IF NOT AN INDIAN USER*/
            if (!isIndianUser) {
                //Keep current flow for non-indian users
                navigateToNextScreen();

            } else {

                //Show a form to Indian Users only
                Intent formIntent = new Intent(this, IndianFormActivity.class);
                startActivity(formIntent);
                finish();

            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /***
     * This will navigate the flow to next screen
     * This action is performed just after an old user is login to the application; we will check if he/she has a valid questionnaire data or not
     * If not then we will navigate it to the Questionnaire page
     */
    private void navigateToNextScreen() {

        Intent intent = new Intent(LoginScreenActivity.this, NavigationScreen.class);
        startActivity(intent);
        finish();

    }

    //Code for MixPanel by IZISS Start

    /***
     * IZISS to set code automatically when come via deeplink
     */
    private boolean isDeepLinkReferralCode() {
        try {

            if (SplashScreenActivity.referralCodeDeepLink != null) {

                String url = SplashScreenActivity.referralCodeDeepLink.toString();

                if (SplashScreenActivity.referralCodeDeepLink.toString().contains("referralCode")) {

                    String values[] = url.split("/");

                    for (int i = 0; i < values.length; i++) {
                        Log.i("Value>", "" + values[i]);
                    }

                    if (values.length > 4) {
                        //It means we have a valid ReferralLink
                        return true;

                    }
                }
            }

            SplashScreenActivity.showToastInBeta(this, "Referral Link on Login:" + SplashScreenActivity.referralCodeDeepLink);

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        //Comment this becasue we will get this link on ReferralCodeScreenActivity as well
        //SplashScreenActivity.referralCodeDeepLink = null;

        return false;

    }

    /***
     * Send Event to mixpanel
     *
     * @param
     */
    private void sendToMixPanel() {

        LoginUser mUser = new SharedPrefrences(this).getLoginUser();

        if (mUser != null) {

            //This object will accept key and its value to send with MixPanel data
            HashMap<String, String> hashMap = new HashMap<>();

            //Key and their values which will be send to Mix Panel
            hashMap.put("DEVICE_ID", mAppUtils.getDeviceID());
            hashMap.put("NAME", mUser.getFullName());
            hashMap.put("EMAIL_ID", mUser.getEmail());

            //Initialize Mixpanel class and sent it
            MixPanelClass mixPanelClass = new MixPanelClass(this);

            //Set clicked variable to true variable
            mixPanelClass.setPref(MixPanelClass.IS_FIRST_CHAPTER, true);
            mixPanelClass.setPref(MixPanelClass.IS_FIRST_VIDEO, true);
            mixPanelClass.sendData(MixPanelClass.MPE_INSTALL_WIHT_NO_REFERRAL_CODE, hashMap);

            //Added on 04 June 2018 to register user just after login on Mixpanel
            //This will set user identity and mix-panel identify user
            //This will set user identity and mix-panel identify user; true means new user / fresh user
            mixPanelClass.setUserIdentity(true);

        }

    }

    /***
     * Check whether a fresh user
     *
     * @return
     */
    private boolean isFreshUserWithNewDevice() {

        if (mSharedPref.getLoginUser().getNewDevice().equals("0") || !mAppUtils.isOwnerUser()) {
            return false;

        } else {

            return true;
        }
    }

    /***
     * This method has the flow of what to do after login
     */
    private void doAfterLogin() {

        //Current flow of the application
        LoginUser User = mSharedPref.getLoginUser();
        Intent intent;

        //if(User.getIsOTPChecked().equals("0") && User.getFriendReferralCode().equals("")){
        if (User.getNewDevice() != null && (User.getNewDevice().equals("1") || User.getNewDevice().equals("0"))) {

            //Change to check whether fresh user has come with a referral code or not to install the application;
            //if not then do not show him ReferalCodeScreenActivity

            //Check fresh user
            if (isFreshUserWithNewDevice()) {

                //Check if he/she has a valid referral code
                if (isDeepLinkReferralCode()) {

                    intent = new Intent(LoginScreenActivity.this, ReferalCodeScreenActivity.class);
                    startActivity(intent);
                    finish();

                } else {

                    //Skip ReferalCodeScreenActivity just get Firebase and Mixpanel attributes from activity
                    new FirebaseClass(this).sendCustomAnalyticsData(FirebaseAnalytics.Event.SIGN_UP, "New Sign Up");

                    //Send data to mixpanel with new event
                    sendToMixPanel();

                    //Check Indian User
                    checkIndianUser();

                }

            } else {
                intent = new Intent(LoginScreenActivity.this, ReferalCodeScreenActivity.class);
                startActivity(intent);
                finish();
            }

        } else {

            //This are older users do not need to set alias for the users just identify them
            //Initialize Mixpanel class and sent it
            MixPanelClass mixPanelClass = new MixPanelClass(this);
            mixPanelClass.setPref(MixPanelClass.IS_VIDEO_SEEN_AFTER_UPDATE, true);
            //mixPanelClass.sendMixPanelPeopleAttribute(false, true, true);

            // Update on 19th June 2019
            mixPanelClass.sendMixPanelPeopleAttribute(false, false, true);

            //If user has already played the questionnaire then the value is not blank
            if (mixPanelClass.isQuestionnaireAnswered()) {
                navigateToNextScreen();
            }
            //We need to check first whether user is an Indian user or not if not then we need to keep the current flow
            else {
                checkIndianUser();

            }
        }
    }
}