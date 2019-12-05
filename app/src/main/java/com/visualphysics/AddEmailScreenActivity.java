package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import org.json.JSONObject;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import zoho.ZohoUtils;

/**
 * Created by India on 7/2/2016.
 */
public class AddEmailScreenActivity extends AppCompatActivity implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private EditText mEdtEmail, mEdtRefferalCode;
    private Button mBtnSubmit, mBtnSkip;
    private String strEmail, strRefferalCode;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;

    private String UserName, Gender, Email, RegisterType, RegisterID;
    private String DeviceModelNumber, DeviceManufacturerName, DeviceOSVersion, DeviceOS = "Android";

    //New text for OTP
    TextView txtOtpSent,txtPleaseProvideEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_email_screen_activity);
        getSupportActionBar().setTitle("Add Info");
        mDeclaration();
    }

    private void mDeclaration() {

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getApplicationContext());
        mProgressDialog = new ProgressDialog(AddEmailScreenActivity.this);
        mProgressDialog.setCancelable(false);

        mEdtEmail = (EditText) findViewById(R.id.edtEmailAddEmailScreen);
        mEdtRefferalCode = (EditText) findViewById(R.id.edtRefferalCodeAddEmailScreen);
        mEdtRefferalCode.setVisibility(View.GONE);

        mBtnSubmit = (Button) findViewById(R.id.btnSubmitAddEmailScreen);
        mBtnSubmit.setOnClickListener(this);

        txtOtpSent = (TextView) findViewById(R.id.txtOtpSent);
        txtPleaseProvideEmail= (TextView) findViewById(R.id.txtPleaseProvideEmail);
        txtOtpSent.setVisibility(View.VISIBLE);
        txtPleaseProvideEmail.setVisibility(View.VISIBLE);

        mBtnSkip = (Button) findViewById(R.id.btnSkipAddEmailScreen);
        mBtnSkip.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent.hasExtra("Name")) {
            UserName = intent.getStringExtra("Name");
            Gender = intent.getStringExtra("Gender");
            RegisterType = intent.getStringExtra("RegisterType");
            RegisterID = intent.getStringExtra("RegisterID");
            if (intent.hasExtra("Email")) { //If Email exits
                strEmail = intent.getStringExtra("Email");
                mEdtEmail.setText(strEmail);
                mEdtEmail.setVisibility(View.GONE);
                mBtnSkip.setVisibility(View.GONE);
            }


        } else {
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    "Please try later");
            finish();
        }

        mEdtRefferalCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.btnSingUp_SignUpScreen || id == EditorInfo.IME_NULL ||
                        id == EditorInfo.IME_ACTION_DONE) {
                    doLoginWithSocialMedia();
                    return true;
                }
                return false;
            }
        });

        getDeviceInfo();

        //Hide Zoho Chat
        ZohoUtils.hideZohoChat();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmitAddEmailScreen:
                doLoginWithSocialMedia();
                break;
            case R.id.btnSkipAddEmailScreen:
                doLoginWithSocialMedia();
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
        if (Method.equals(mApiCall.LoginWithSocialMedia)) {
            parseResponseForAddEmailData(result);
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


    private void doLoginWithSocialMedia() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        if (checkValidation()) {
            if (!mAppUtils.getConnectionState()) {
                mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
            } else {
                String UserDeviceType = "Primary";
                if (!mAppUtils.isOwnerUser())
                    UserDeviceType = "Secondary";

                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();
                int OtpCheckAllow = 2; //When user add Email Address Manully then we have to validate email by OTP
                mApiCall.doLoginWithSocialMedia(UserName, strEmail, "101", DeviceManufacturerName + " " + DeviceModelNumber,
                        DeviceOS, DeviceOSVersion, mAppUtils.getDeviceID(), RegisterType, RegisterID,
                        strRefferalCode, String.valueOf(mAppUtils.getAppVersionCode()), Build.SERIAL, UserDeviceType, OtpCheckAllow,
                        new OnTaskCompleted(this), mApiCall.LoginWithSocialMedia);
            }
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
     * Check the Validation of the input
     *
     * @return
     */
    private boolean checkValidation() {
        boolean status = false;
        strEmail = mEdtEmail.getText().toString().trim();
        strRefferalCode = mEdtRefferalCode.getText().toString();
        mEdtEmail.setError(null);
        if (!TextUtils.isEmpty(strEmail)) {
            if (mAppUtils.isEmailValid(strEmail))
                status = true;
            else {
                mEdtEmail.setError(getResources().getString(R.string.error_invalid_email));
            }
        } else {
            mEdtEmail.setError(getResources().getString(R.string.error_field_required));
        }

        return status;
    }


    /***
     * Parse the data of add email data
     *
     * @param response
     */
    private void parseResponseForAddEmailData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.LoginWithSocialMedia);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                mAppUtils.displayToastWithMessage(getApplicationContext(),
                        mJsonObj.getString("Message"));

                SharedPrefrences mSharedPref = new SharedPrefrences(getApplicationContext());
                mSharedPref.setPreferences(mSharedPref.USERDETAIL, mJsonObj.getJSONObject("data").toString());

                //Video base URL Detail
                mSharedPref.setPreferences(mSharedPref.STREAM_URL, mJsonObj.getJSONObject("config").getString("OnlineURL"));
                mSharedPref.setPreferences(mSharedPref.DOWNLOAD_URL, mJsonObj.getJSONObject("config").getString("OfflineURL"));
                mSharedPref.setPreferences(mSharedPref.SUPPORT_EMAIL, mJsonObj.getJSONObject("config").getString("SupportEmail"));
                mSharedPref.setPreferences(mSharedPref.APP_VERSION, mJsonObj.getJSONObject("config").getString("AppVersion"));


               /* Intent intent = new Intent(AddEmailScreenActivity.this, OTPScreen.class);
                startActivity(intent);
                finish();*/

                LoginUser User = mSharedPref.getLoginUser();
                Intent intent;
                if (User.getNewDevice() != null
                        && (User.getNewDevice().equals("1") || User.getNewDevice().equals("0") ||
                        mSharedPref.getLoginUser().getIsOTPChecked().equals("0"))) {

                    /*Commented by IZISS to change flow of verification*/
                    /*intent = new Intent(AddEmailScreenActivity.this, ReferalCodeScreenActivity.class);
                    startActivity(intent);
                    finish();*/

                    if (User.getIsOTPChecked().equals("0")) {
                        intent = new Intent(AddEmailScreenActivity.this, OTPScreen.class);
                        startActivity(intent);
                       // finish();
                    }

                } else {
                    intent = new Intent(AddEmailScreenActivity.this, NavigationScreen.class);
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

}
