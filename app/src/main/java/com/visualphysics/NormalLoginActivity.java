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

import com.android.volley.VolleyError;
import com.facebook.FacebookSdk;
import com.google.gson.Gson;

import org.json.JSONObject;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;

/**
 * Created by iziss on 17/11/17.
 */
public class NormalLoginActivity extends AppCompatActivity implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private EditText mEdtEmail, mEdtPassword;

    private String DeviceModelNumber, DeviceManufacturerName, DeviceOSVersion, DeviceOS = "Android";
    private String strEmail, strPassword;
    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private SharedPrefrences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_normal_login);

        getSupportActionBar().setTitle("Login");


        mDeclaration();
        getDeviceInfo();

    }

    void mDeclaration() {

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getApplicationContext());
        mProgressDialog = new ProgressDialog(NormalLoginActivity.this);
        mProgressDialog.setCancelable(false);
        mSharedPref = new SharedPrefrences(getApplicationContext());

        mEdtEmail = (EditText) findViewById(R.id.edtUserNameLoginScreen);
        mEdtPassword = (EditText) findViewById(R.id.edtPasswordLoginScreen);

        Button mLoginButton = (Button) findViewById(R.id.btnLogin_LoginScreen);
        Button mForgotpass = (Button) findViewById(R.id.btnForgotPasswordLoginScreen);


        mLoginButton.setOnClickListener(this);
        mForgotpass.setOnClickListener(this);

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
        switch (v.getId()) {
            case R.id.btnLogin_LoginScreen:
                doLogin();
                break;

            case R.id.btnForgotPasswordLoginScreen:
                intent = new Intent(NormalLoginActivity.this, ForgotPasswordScreen.class);
                startActivity(intent);
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.Login)) {
            parseResponseForLoginData(result);
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
            new AlertDialog.Builder(NormalLoginActivity.this, R.style.AppCompatAlertDialogStyle)
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
                    Intent intent = new Intent(NormalLoginActivity.this, OTPScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(NormalLoginActivity.this, NavigationScreen.class);
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
