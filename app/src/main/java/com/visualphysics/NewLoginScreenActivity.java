package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ApplicationConfiguration;
import Utils.DebugLog;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import firebase.FirebaseClass;

/**
 * Created by iziss on 17/11/17.
 */
public class NewLoginScreenActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, OnTaskCompleted.CallBackListener {

    private EditText mEdtEmail, mEdtPassword;
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
        setContentView(R.layout.activity_login_screen);
        mDeclaration();
        getDeviceInfo();



    }

    void mDeclaration() {
        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getApplicationContext());
        mProgressDialog = new ProgressDialog(NewLoginScreenActivity.this);
        mProgressDialog.setCancelable(false);
        mSharedPref = new SharedPrefrences(getApplicationContext());

        mEdtEmail = (EditText) findViewById(R.id.edtUserNameLoginScreen);
        mEdtPassword = (EditText) findViewById(R.id.edtPasswordLoginScreen);

        Button mLoginButton = (Button) findViewById(R.id.btnLogin_LoginScreen);
        Button mForgotpass = (Button) findViewById(R.id.btnForgotPasswordLoginScreen);
        Button mSignUp = (Button) findViewById(R.id.btnSignUpLoginScreen);
        Button mImgBtnGPlus = (Button) findViewById(R.id.imgBtnGooglePlusLoginScreen);
        Button mImgBtnFacebook = (Button) findViewById(R.id.imgBtnFacebookLoginScreen);
        mLoginButton.setOnClickListener(this);
        mForgotpass.setOnClickListener(this);
        mSignUp.setOnClickListener(this);
        mImgBtnGPlus.setOnClickListener(this);
        mImgBtnFacebook.setOnClickListener(this);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mEdtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.edtPasswordLoginScreen || id == EditorInfo.IME_NULL ||
                        id == EditorInfo.IME_ACTION_DONE) {
                    doLogin();
                    return true;
                }
                return false;
            }
        });

        //Check is user try to login with guest user
        checkIsSecondaryDevice();


    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        LoginWithFacebook = false;
        switch (v.getId()) {
            case R.id.btnLogin_LoginScreen:
                doLogin();
                break;

            case R.id.btnForgotPasswordLoginScreen:
                intent = new Intent(NewLoginScreenActivity.this, ForgotPasswordScreen.class);
                startActivity(intent);
                break;


            case R.id.btnSignUpLoginScreen:
                intent = new Intent(NewLoginScreenActivity.this, SignUpScreen.class);
                startActivity(intent);

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
            new AlertDialog.Builder(NewLoginScreenActivity.this, R.style.AppCompatAlertDialogStyle)
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
     * Attempt Login
     */
    public void doLogin() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        String DeviceID = mAppUtils.getDeviceID();
        if (checkValidation()) {
            if (!mAppUtils.getConnectionState()) {
                mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
            } else if (DeviceID == null)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content), getResources().getString(R.string.error_device_id_not_found));
            else {
                String UserDeviceType = "Primary";
                if (!mAppUtils.isOwnerUser())
                    UserDeviceType = "Secondary";

                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();

                mApiCall.doLogin(strEmail, strPassword, DeviceManufacturerName, DeviceOS,
                        DeviceOSVersion, DeviceID, String.valueOf(mAppUtils.getAppVersionCode()), Build.SERIAL,
                        UserDeviceType,
                        new OnTaskCompleted(this), mApiCall.Login);

            }
        }
    }

    /***
     * Login with Social Media
     */
    private void doLoginWithSocialMedia() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
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
     * Check validation for the sign up
     *
     * @return
     */
    private boolean checkValidation() {
        boolean status = false;

        mEdtEmail.setError(null);
        mEdtPassword.setError(null);

        strEmail = mEdtEmail.getText().toString().trim();
        strPassword = mEdtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(strEmail))
            mEdtEmail.setError(getResources().getString(R.string.error_field_required));
        if (TextUtils.isEmpty(strPassword))
            mEdtPassword.setError(getResources().getString(R.string.error_field_required));

        if (!mAppUtils.isEmailValid(strEmail)) {
            mEdtEmail.setError(getResources().getString(R.string.error_invalid_email));
        } else if (strPassword.length() < 6) {
            mEdtPassword.setError(getResources().getString(R.string.error_invalid_password));
        } else {
            status = true;
        }

        return status;
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
                    Intent intent = new Intent(NewLoginScreenActivity.this, OTPScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(NewLoginScreenActivity.this, NavigationScreen.class);
                    startActivity(intent);
                    finish();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
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
                Intent i = new Intent(getApplicationContext(), AddEmailScreenActivity.class);
                i.putExtra("RegisterType", RegisterType);
                i.putExtra("RegisterID", RegisterID);
                i.putExtra("Gender", strGender);
                i.putExtra("Name", strUserName);
                if (strEmail != null && !strEmail.equals("")) {
                    i.putExtra("Email", strEmail);
                }
                startActivity(i);
                finish();
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

                LoginUser User = mSharedPref.getLoginUser();
                Intent intent;
                //if(User.getIsOTPChecked().equals("0") && User.getFriendReferralCode().equals("")){
                if (User.getNewDevice() != null && (User.getNewDevice().equals("1") || User.getNewDevice().equals("0"))) {
                    if (User.getNewDevice().equals("0")) {
                        new FirebaseClass(NewLoginScreenActivity.this).sendCustomAnalyticsData(FirebaseAnalytics.Event.SIGN_UP, "New Sign Up");
                    }

                    intent = new Intent(NewLoginScreenActivity.this, ReferalCodeScreenActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    /*// If user register successfully but not verify the OTP then move to OTP screen
                    if(User.getIsOTPChecked().equals("0")){
                        intent = new Intent(LoginScreenActivity.this, OTPScreen.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        intent = new Intent(LoginScreenActivity.this, NavigationScreen.class);
                        startActivity(intent);
                        finish();
                    }*/
                    intent = new Intent(NewLoginScreenActivity.this, NavigationScreen.class);
                    startActivity(intent);
                    finish();

                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
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
                                            DebugLog.v("Face", object.toString());
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
                        parameters.putString("fields", "id,name,email,gender");
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
            ErrorLog.SendErrorReport(e);
        }
    }


    //Add the count when the app open
    private void addAppOpenCount() {
        mSharedPref.setPreferences(mSharedPref.APP_OPEN_COUNT, 1);
    }

}