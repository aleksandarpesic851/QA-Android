package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.intertrust.wasabi.drm.User;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import firebase.FirebaseClass;
import mixpanel.MixPanelClass;
import zoho.ZohoUtils;

/**
 * Created by India on 7/2/2016.
 */
public class ReferalCodeScreenActivity extends AppCompatActivity implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private EditText mEdtEmail, mEdtRefferalCode;
    private Button mBtnSubmit, mBtnSkip;
    private String strEmail, strRefferalCode;
    private TextView txtReferralCode;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private SharedPrefrences mSharePref;

    private String DeviceModelNumber, DeviceManufacturerName, DeviceOSVersion;

    public static String KEY_INDIA_COUNTRY_CODE = "countryCode";
    public static String KEY_INDIA_COUNTRY_NAME = "country";

    public static String INDIA_COUNTRY_CODE = "IN";
    public static String INDIA_COUNTRY_NAME = "India";

    boolean isReferralCodeApplied = false;
    private boolean isFreshUserWithNewEmailID = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_email_screen_activity);
        getSupportActionBar().setTitle("Add Referral Code");
        mDeclaration();

        //Hide Zoho Chat
        ZohoUtils.hideZohoChat();
    }

    private void mDeclaration() {

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getApplicationContext());
        mSharePref = new SharedPrefrences(getApplicationContext());
        mProgressDialog = new ProgressDialog(ReferalCodeScreenActivity.this);
        mProgressDialog.setCancelable(false);

        mEdtEmail = (EditText) findViewById(R.id.edtEmailAddEmailScreen);
        mEdtRefferalCode = (EditText) findViewById(R.id.edtRefferalCodeAddEmailScreen);

        txtReferralCode = (TextView) findViewById(R.id.txtReferralCode);

        mBtnSubmit = (Button) findViewById(R.id.btnSubmitAddEmailScreen);
        mBtnSubmit.setOnClickListener(this);

        mBtnSkip = (Button) findViewById(R.id.btnSkipAddEmailScreen);
        mBtnSkip.setOnClickListener(this);

        mEdtEmail.setVisibility(View.GONE);
        mBtnSkip.setVisibility(View.VISIBLE);
        txtReferralCode.setVisibility(View.VISIBLE);

        mEdtRefferalCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.btnSingUp_SignUpScreen || id == EditorInfo.IME_NULL ||
                        id == EditorInfo.IME_ACTION_DONE) {
                    //doLoginWithSocialMedia();
                    doAddRefralCode();
                    return true;
                }
                return false;
            }
        });

        getDeviceInfo();

        //This will set referral code on edit text
        setDeepLinkReferralCode();

        isReferralGiveToUser();

        //This will send sign up report to firebase
        new FirebaseClass(this).sendCustomAnalyticsData(FirebaseAnalytics.Event.SIGN_UP, "New Sign Up");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnSubmitAddEmailScreen:
                doAddRefralCode();
                break;

            case R.id.btnSkipAddEmailScreen:

            case R.id.btnNextAddEmailScreen:

                // This method will be used for above commented code and its result will handle navigation
                checkIndianUser(false);

                break;

            default:
                break;
        }
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.AddReferalCode)) {
            parseResponseForAddReferalCodeData(result);
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
        mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content), getResources().getString(R.string.Error_Msg_Try_Later));
    }

    private void doAddRefralCode() {

        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

            if (imm != null)
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }

        if (checkValidation()) {
            if (!mAppUtils.getConnectionState()) {
                mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
            } else {
                LoginUser User = mSharePref.getLoginUser();
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();
                mApiCall.doAddReferalCode(User.getStudentID(), strRefferalCode,
                        new OnTaskCompleted(this), mApiCall.AddReferalCode);
            }
        }
    }

    /***
     * Get the device info
     */
    private void getDeviceInfo() {

        DeviceManufacturerName = Build.MANUFACTURER;
        DeviceModelNumber = Build.MODEL;
        DeviceOSVersion = Build.VERSION.RELEASE;
    }

    /***
     * Check user should allow to add referral and the bases of device
     * If device is already register with the system then don't allow user to add reffreal code
     */
    private void isReferralGiveToUser() {

        if (mSharePref == null)
            mSharePref = new SharedPrefrences(getApplicationContext());

        try {

            //When a new user login in same device, with new email id
            if (mSharePref.getLoginUser().getNewDevice().equals("0")) {
                mEdtRefferalCode.setVisibility(View.GONE);
                txtReferralCode.setVisibility(View.GONE);
                ((RelativeLayout) findViewById(R.id.rLayoutDeviceAlreadyRegViewAddEmailScreen)).setVisibility(View.VISIBLE);
                ((LinearLayout) findViewById(R.id.lLayoutButtonViewAddEmailScreen)).setVisibility(View.GONE);

                ((Button) findViewById(R.id.btnNextAddEmailScreen)).setOnClickListener(this);

                sendToMixPanel(false);

                //Set variable to show different texts on Free Trial Screen
                MixPanelClass.IS_REPEAT_LOGIN_WITH_DIFFERENT_EMAIL = true;

            } else if (!mAppUtils.isOwnerUser()) {
                mEdtRefferalCode.setVisibility(View.GONE);
                txtReferralCode.setVisibility(View.GONE);
                ((RelativeLayout) findViewById(R.id.rLayoutDeviceAlreadyRegViewAddEmailScreen)).setVisibility(View.VISIBLE);
                ((LinearLayout) findViewById(R.id.lLayoutButtonViewAddEmailScreen)).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.txtMsgAddEmailScreen)).setText(getString(R.string.msg_seconday_device_free_trial));
                ((Button) findViewById(R.id.btnNextAddEmailScreen)).setOnClickListener(this);

                sendToMixPanel(false);

                //Set variable to show different texts on Free Trial Screen
                MixPanelClass.IS_REPEAT_LOGIN_WITH_DIFFERENT_EMAIL = true;

            } else {

                //Fresh User
                sendToMixPanel(true);

                //Set variable to show different texts on Free Trial Screen
                MixPanelClass.IS_REPEAT_LOGIN_WITH_DIFFERENT_EMAIL = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SendErrorReport(e);
        }
    }

    /***
     * Check the Validation of the input
     *
     * @return
     */
    private boolean checkValidation() {
        boolean status = false;
        strRefferalCode = mEdtRefferalCode.getText().toString();
        if (!TextUtils.isEmpty(strRefferalCode)) {
            status = true;
        } else {
            mEdtRefferalCode.setError(getResources().getString(R.string.error_field_required));
        }

        return status;
    }

    /***
     * Parse the data to add email data
     *
     * @param response
     */
    private void parseResponseForAddReferalCodeData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AddReferalCode);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {

                mAppUtils.displayToastWithMessage(getApplicationContext(),
                        mJsonObj.getString("Message"));

                //Commented by IZISS on 20 Feb to show Indian Form
                /*LoginUser User = mSharePref.getLoginUser();
                User.setFriendReferralCode(strRefferalCode); //Update NewDevice to Null so that at the Time of Splash Screen(Ref MoveTONextScreen()) consider as old device
                User.setNewDevice(null);

                Gson gson = new Gson();
                String jsonString = gson.toJson(User);

                //Update User Detail
                mSharePref.setPreferences(mSharePref.USERDETAIL, jsonString);

                Intent intent;
                if (User.getIsOTPChecked().equals("0")) {
                    intent = new Intent(ReferalCodeScreenActivity.this, OTPScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    intent = new Intent(ReferalCodeScreenActivity.this, NavigationScreen.class);
                    startActivity(intent);
                    finish();
                }*/

                //This method will be used for above commented code and its result will handle navigation
                checkIndianUser(true);


            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
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
     * IZISS to set code automatically when come via deeplink
     */
    private void setDeepLinkReferralCode() {
        try {

            if (SplashScreenActivity.referralCodeDeepLink != null) {

                String url = SplashScreenActivity.referralCodeDeepLink.toString();

                if (SplashScreenActivity.referralCodeDeepLink.toString().contains("referralCode")) {

                    String values[] = url.split("/");

                    for (int i = 0; i < values.length; i++) {
                        Log.i("Value>", "" + values[i]);
                    }

                    if (values.length > 4) {
                        mEdtRefferalCode.setText(values[4]);

                    }

                }
            }
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        SplashScreenActivity.referralCodeDeepLink = null;

    }

    /***
     * Send Event to mixpanel
     *
     * @param isNewUserWithNewDevice
     */
    private void sendToMixPanel(boolean isNewUserWithNewDevice) {

        /*Code for MixPanel by IZISS Start*/
        {
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

                //If a fresh user
                if (isNewUserWithNewDevice) {

                    //Set clicked variable to true variable
                    mixPanelClass.setPref(MixPanelClass.IS_FIRST_CHAPTER, true);
                    mixPanelClass.setPref(MixPanelClass.IS_FIRST_VIDEO, true);

                    isFreshUserWithNewEmailID = true;

                    //Send data to our function which will be further sent to Mix Panel
                    mixPanelClass.sendData(MixPanelClass.MPE_REFERRAL_CODE_SCREEN, hashMap);

                    if (mEdtRefferalCode.getText().toString().length() > 0) {

                        //Implement automatic click when referral code is pre-filled
                        goToNext();
                    }


                } else {

                    //If user login with different email id on same device it will be treated as re-login with different email id
                    HashMap<String, String> hashMapEmail = new HashMap<>();
                    hashMapEmail.put("New_Email", mUser.getEmail());
                    mixPanelClass.sendData(MixPanelClass.MPE_LOGIN, hashMapEmail);

                    isFreshUserWithNewEmailID = false;

                    //Added to send No referral to mixpanel on 01 June 2018
                    //Send data to our function which will be further sent to Mix Panel
                    mixPanelClass.sendData(MixPanelClass.MPE_NO_REFERRAL, hashMap);

                }

                //Added on 04 June 2018 to register user just after login on Mixpanel
                //This will set user identity and mix-panel identify user
                mixPanelClass.setUserIdentity(isNewUserWithNewDevice);

            }

        } /*Code for MixPanel by IZISS End*/
    }


    /**
     * Detect if the phone's IP is in INDIA COUNTRY to show a form to user other wise move ahead
     *
     * @return true->For Indian Users; false-> Users of other countries (will keep current flow of application)
     */

    private void checkIndianUser(boolean isReferralCodeApplied) {

        /*//Show next Questionnaire only when a fresh user is login not a repeat login with new email id
        if (!isFreshUserWithNewEmailID) {

            //Keep current flow for non-indian users
            navigateToNextScreen();

        } else {

            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            this.isReferralCodeApplied = isReferralCodeApplied;

            mApiCall.getLocationFromIpAddress(new OnTaskCompleted(this), mApiCall.GetLocation);

        }*/

        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.show();

        this.isReferralCodeApplied = isReferralCodeApplied;

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

                if (response.optString(KEY_INDIA_COUNTRY_NAME).equalsIgnoreCase(INDIA_COUNTRY_NAME) && response.optString(KEY_INDIA_COUNTRY_CODE).equalsIgnoreCase(INDIA_COUNTRY_CODE)) {
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

                if (isReferralCodeApplied) {
                    formIntent.putExtra("IS_REFERRAL_CODE_APPLIED", isReferralCodeApplied);
                    formIntent.putExtra("REFERRAL_CODE", strRefferalCode);
                }

                startActivity(formIntent);
                finish();

            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /***
     * This will navigate the flow to next screen
     */
    private void navigateToNextScreen() {

        LoginUser User = mSharePref.getLoginUser();

        if (isReferralCodeApplied)
            User.setFriendReferralCode(strRefferalCode);

        //Update NewDevice to Null so that at the Time of Splash Screen(Ref MoveTONextScreen()) consider as old device
        User.setNewDevice(null);

        Gson gson = new Gson();
        String jsonString = gson.toJson(User);

        //Update User Detail
        mSharePref.setPreferences(mSharePref.USERDETAIL, jsonString);

        //Intent to start new activity
        Intent intent;

        if (User.getIsOTPChecked().equals("0")) {
            intent = new Intent(ReferalCodeScreenActivity.this, OTPScreen.class);
            startActivity(intent);
            finish();
        } else {
            intent = new Intent(ReferalCodeScreenActivity.this, NavigationScreen.class);
            startActivity(intent);
            finish();
        }

    }

    /***
     * Implement automatic click when referral code is not available
     */
    private void skipStep() {

        //mBtnSkip.performClick();
    }

    /***
     * Implement automatic click when referral code is not available
     */
    private void goToNext() {

        mBtnSubmit.performClick();
    }

}
