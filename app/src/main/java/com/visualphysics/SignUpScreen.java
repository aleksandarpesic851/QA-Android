package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;

import Database.DataBase;
import Model.Countries;
import Utils.AppUtil;
import Utils.ApplicationConfiguration;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;

public class SignUpScreen extends AppCompatActivity implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private EditText mEdtFullName, mEdtEmail, mEdtConfirmEmail, mEdtPhoneNo, mEdtPassword, mEdtConfirmPassword,
            mEdtRefferalCode;
    private Spinner mSpinnerCountry;
    private Button mBtnSignUp;

    private ArrayList<Countries> mCountryArrayList;
    private String strFullName, strEmail, strConfirmEmail, strPhoneNo, strPassword, strConfirmPassword, strCountryID,
            strRefferalCode;
    private String DeviceModelNumber, DeviceManufacturerName, DeviceOSVersion, DeviceOS = "Android";
    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private SharedPrefrences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        getSupportActionBar().setTitle("Sign Up");

        mDeclaration();

    }

    void mDeclaration() {
        DataBase db = new DataBase(getApplicationContext());
        try {
            db.createDataBase();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getApplicationContext());
        mSharedPref = new SharedPrefrences(getApplicationContext());
        mProgressDialog = new ProgressDialog(SignUpScreen.this);
        mProgressDialog.setCancelable(false);

        mEdtFullName = (EditText) findViewById(R.id.edtFirstNameSignUpScreen);
        mEdtEmail = (EditText) findViewById(R.id.edtEmailSignUpScreen);
        mEdtConfirmEmail = (EditText) findViewById(R.id.edtConfirmEmailSignUpScreen);
        mEdtPhoneNo = (EditText) findViewById(R.id.edtPhoneNoSignUpScreen);
        mEdtPassword = (EditText) findViewById(R.id.edtPasswordSignUpScreen);
        mEdtConfirmPassword = (EditText) findViewById(R.id.edtConfirmPasswordSignUpScreen);
        mEdtRefferalCode = (EditText) findViewById(R.id.edtRefferalCodeSignUpScreen);

        mBtnSignUp = (Button) findViewById(R.id.btnSingUp_SignUpScreen);
        mBtnSignUp.setOnClickListener(this);

        mSpinnerCountry = (Spinner) findViewById(R.id.spinnerCountrySignUpScreen);

        mCountryArrayList = db.getCountryList();
        if (mCountryArrayList == null)
            mCountryArrayList = new ArrayList<Countries>();

        mCountryArrayList.add(0, new Countries("-1", "Select Country"));
        ArrayAdapter<Countries> dataAdapter = new ArrayAdapter<Countries>(getApplicationContext(),
                R.layout.spinner_listitem, mCountryArrayList);
        mSpinnerCountry.setAdapter(dataAdapter);

        mEdtConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.edtConfirmPasswordSignUpScreen || id == EditorInfo.IME_NULL ||
                        id == EditorInfo.IME_ACTION_DONE) {
                    doSignUp();
                    return true;
                }
                return false;
            }
        });
        mEdtConfirmEmail.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        mEdtEmail.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });

        getDeviceInfo();
        setListeners();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSingUp_SignUpScreen:
                doSignUp();
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
        if (Method.equals(mApiCall.SignUp)) {
            parseResponseForSignUpData(result);
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
     * Get the device info
     */
    private void getDeviceInfo() {
        DeviceManufacturerName = Build.MANUFACTURER;
        DeviceModelNumber = Build.MODEL;
        DeviceOSVersion = android.os.Build.VERSION.RELEASE;

        //If user sign with seconday user then hide the refer code text
        if (!mAppUtils.isOwnerUser())
            mEdtRefferalCode.setVisibility(View.GONE);

    }

    /***
     * Set the Listeners that are used in SignUp screen
     */
    private void setListeners() {
        mSpinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEdtRefferalCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.btnSingUp_SignUpScreen || id == EditorInfo.IME_NULL ||
                        id == EditorInfo.IME_ACTION_DONE) {
                    doSignUp();
                    return true;
                }
                return false;
            }
        });

        mSpinnerCountry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });
    }

    /***
     * Attemp SignUp
     */
    public void doSignUp() {
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
                mApiCall.doSignUp(strFullName, strEmail, strPassword, strPhoneNo, strCountryID,
                        DeviceManufacturerName + " " + DeviceModelNumber, DeviceOS,
                        DeviceOSVersion, DeviceID, ApplicationConfiguration.UserRegType.Email.toString(),
                        "", strRefferalCode, String.valueOf(mAppUtils.getAppVersionCode()), Build.SERIAL,
                        UserDeviceType,
                        new OnTaskCompleted(this), mApiCall.SignUp);
            }
        }
    }

    /***
     * Check validation for the sign up
     *
     * @return
     */
    private boolean checkValidation() {
        boolean status = false, isRequiredField = true;

        mEdtFullName.setError(null);
        mEdtEmail.setError(null);
        mEdtPhoneNo.setError(null);
        mEdtPassword.setError(null);
        mEdtConfirmPassword.setError(null);
        mEdtConfirmEmail.setError(null);

        strFullName = mEdtFullName.getText().toString().trim();
        strEmail = mEdtEmail.getText().toString().trim();
        strConfirmEmail = mEdtConfirmEmail.getText().toString().trim();
        strPhoneNo = mEdtPhoneNo.getText().toString().trim();
        strPassword = mEdtPassword.getText().toString().trim();
        strConfirmPassword = mEdtConfirmPassword.getText().toString().trim();
        strRefferalCode = mEdtRefferalCode.getText().toString().trim();
        Countries selectedCountry = (Countries) mSpinnerCountry.getSelectedItem();


        if (TextUtils.isEmpty(strFullName)) {
            mEdtFullName.setError(getResources().getString(R.string.error_field_required));
            isRequiredField = false;
        }
        if (TextUtils.isEmpty(strEmail)) {
            mEdtEmail.setError(getResources().getString(R.string.error_field_required));
            isRequiredField = false;
        }
        if (TextUtils.isEmpty(strPhoneNo)) {
            mEdtPhoneNo.setError(getResources().getString(R.string.error_field_required));
            isRequiredField = false;
        }
        if (TextUtils.isEmpty(strPassword)) {
            mEdtPassword.setError(getResources().getString(R.string.error_field_required));
            isRequiredField = false;
        }
        if (TextUtils.isEmpty(strConfirmPassword)) {
            mEdtConfirmPassword.setError(getResources().getString(R.string.error_field_required));
            isRequiredField = false;
        }
        if (TextUtils.isEmpty(strConfirmEmail)) {
            mEdtConfirmEmail.setError(getResources().getString(R.string.error_field_required));
            isRequiredField = false;
        }

        if (isRequiredField) {
            if (!mAppUtils.isEmailValid(strEmail)) {
                mEdtEmail.setError(getResources().getString(R.string.error_invalid_email));
            } else if (!strEmail.equals(strConfirmEmail))
                mEdtConfirmEmail.setError(getResources().getString(R.string.error_incorrect_email));
            else if (strPassword.length() < 6) {
                mEdtPassword.setError(getResources().getString(R.string.error_invalid_password));
            } else if (!strPassword.equals(strConfirmPassword))
                mEdtConfirmPassword.setError(getResources().getString(R.string.error_incorrect_password));
            else if (selectedCountry.getCountryID().equals("-1"))
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        "Please select country");
            else {
                strCountryID = selectedCountry.getCountryID();
                status = true;
            }
        }
        return status;
    }

    /***
     * Parse the of
     *
     * @param response
     */
    private void parseResponseForSignUpData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.SignUp);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
               /* mAppUtils.displayToastWithMessage(getApplicationContext(),
                        mJsonObj.getString("Message"));*/

                //User Login Detail
                mSharedPref.setPreferences(mSharedPref.USERDETAIL, mJsonObj.getJSONObject("data").toString());

                //Video base URL Detail
                mSharedPref.setPreferences(mSharedPref.STREAM_URL, mJsonObj.getJSONObject("config").getString("OnlineURL"));
                mSharedPref.setPreferences(mSharedPref.DOWNLOAD_URL, mJsonObj.getJSONObject("config").getString("OfflineURL"));
                mSharedPref.setPreferences(mSharedPref.SUPPORT_EMAIL, mJsonObj.getJSONObject("config").getString("SupportEmail"));
                mSharedPref.setPreferences(mSharedPref.APP_VERSION, mJsonObj.getJSONObject("config").getString("AppVersion"));

                //Intent intent = new Intent(SignUpScreen.this, OTPScreen.class);
                Intent intent = new Intent(SignUpScreen.this, ReferalCodeScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
            ErrorLog.SendSignUpErrorReport(e);
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
