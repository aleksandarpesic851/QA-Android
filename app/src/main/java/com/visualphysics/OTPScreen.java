package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.json.JSONObject;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import zoho.ZohoUtils;

public class OTPScreen extends AppCompatActivity implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private ApiCall mApiCall;
    private EditText mEdt_OTPScreenOTP;
    private Button mBtn_OTPScreenSubmit, mBtnRegenrateOTP;
    private String strOtpNumber;
    private ProgressDialog mProgressDialog;
    private AppUtil mAppUtils;
    private SharedPrefrences mSharedPref;
    private boolean isPageLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpscreen);

        getSupportActionBar().setTitle("OTP ");

        mDecalaration();
    }

    /***
     * Initialize resources
     */
    void mDecalaration() {

        mApiCall = new ApiCall();
        mEdt_OTPScreenOTP = (EditText) findViewById(R.id.edt_OTPScreenOTP);
        mAppUtils = new AppUtil(OTPScreen.this);
        mBtn_OTPScreenSubmit = (Button) findViewById(R.id.btn_OTPScreenSubmit);

        mBtn_OTPScreenSubmit.setOnClickListener(this);
        mProgressDialog = new ProgressDialog(OTPScreen.this);
        mProgressDialog.setCancelable(false);

        mBtnRegenrateOTP = (Button) findViewById(R.id.btnRegenrateOTPScreenSubmit);
        mBtnRegenrateOTP.setOnClickListener(this);

        mSharedPref = new SharedPrefrences(getApplicationContext());

        //callForOTP();
        regenrateOTP();

        //Hide Zoho Chat
        ZohoUtils.hideZohoChat();
    }


    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(getApplicationContext(),LoginScreenActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }*/


    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.GetOtpNumber)) {
            parseResponseForGetOtpNumber(result);
        } else if (Method.equals(mApiCall.CheckOtpNumber)) {
            parseResponseForCheckOtpNumber(result);
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

    @Override
    public void onClick(View v) {
        InputMethodManager imm;
        switch (v.getId()) {
            case R.id.btn_OTPScreenSubmit:
                imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (checkValidation()) {
                    if (!mAppUtils.getConnectionState()) {
                        mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
                    } else {

                        mProgressDialog.setMessage("Please wait...");
                        mProgressDialog.show();
                        mApiCall.checkOtpNumber(mSharedPref.getLoginUser().getStudentID(),
                                strOtpNumber, mAppUtils.getDeviceID(),
                                new OnTaskCompleted(this), mApiCall.CheckOtpNumber);

                    }
                }
                break;
            case R.id.btnRegenrateOTPScreenSubmit:
                imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (!mAppUtils.getConnectionState()) {
                    mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
                } else {
                    regenrateOTP();
                }
                break;
        }
    }


    /***
     * Check the Validation of the input
     *
     * @return
     */
    private boolean checkValidation() {
        boolean status = false;
        strOtpNumber = mEdt_OTPScreenOTP.getText().toString().trim();
        mEdt_OTPScreenOTP.setError(null);
        if (!TextUtils.isEmpty(strOtpNumber)) {

            status = true;
        } else {
            mEdt_OTPScreenOTP.setError(getResources().getString(R.string.error_field_required));
        }

        return status;
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
     * Call Api OTP
     */
    void regenrateOTP() {
        if (isPageLoad) {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
        }
        mApiCall.getOtpNumber(mSharedPref.getLoginUser().getStudentID(),
                new OnTaskCompleted(this), mApiCall.GetOtpNumber);
        isPageLoad = true;
    }


    /***
     * @param response
     */

    private void parseResponseForGetOtpNumber(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetOtpNumber);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
                //mAppUtils.displayAlert("New OTP : "+mJsonObj.getJSONObject("data").getString("NewOTP"));

                // jsonArray.getJSONObject(0).getString("NewOTP");

//                mEdt_OTPScreenOTP.setText();

                //   mEdt_OTPScreenOTP.setText(jsonArray.getJSONObject(0).getString("NewOTP"));
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
     * @param response
     */


    private void parseResponseForCheckOtpNumber(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.CheckOtpNumber);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));

                LoginUser User = mSharedPref.getLoginUser();
                User.setIsOTPChecked("1");

                Gson gson = new Gson();
                String jsonString = gson.toJson(User);

                //Update User Detail
                mSharedPref.setPreferences(mSharedPref.USERDETAIL, jsonString);


               /*Commented by IZISS to change flow of verification*/
                //Intent intent = new Intent(OTPScreen.this,NavigationScreen.class);
                Intent intent = new Intent(OTPScreen.this, ReferalCodeScreenActivity.class);
                startActivity(intent);
                finish();


            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

}
