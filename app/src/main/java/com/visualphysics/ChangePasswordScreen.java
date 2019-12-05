package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;

public class ChangePasswordScreen extends AppCompatActivity implements OnTaskCompleted.CallBackListener {

    private EditText mEdtOldPassword, mEdtNewPassword, mEdtConfirmNewPassword;
    private Button mBtnSubmit;
    private String strOldPassword, strNewPassword, strConfirmNewPassword;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private SharedPrefrences mSharePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_screen);

        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDeclaration();
    }

    private void mDeclaration() {

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getApplicationContext());
        mProgressDialog = new ProgressDialog(ChangePasswordScreen.this);
        mProgressDialog.setCancelable(false);

        mEdtOldPassword = (EditText) findViewById(R.id.edtOldPasswordChangePasswordScreen);
        mEdtNewPassword = (EditText) findViewById(R.id.edtNewPasswordChangePasswordScreen);
        mEdtConfirmNewPassword = (EditText) findViewById(R.id.edtConfirmPasswordChangePasswordScreen);

        mBtnSubmit = (Button) findViewById(R.id.btnSubmitChangePasswordScreen);
        setListeners();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.ChangePassword)) {
            parseResponseForChangePasswordData(result);
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
     * Set the Listeners that are used in SignUp screen
     */
    private void setListeners() {
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doChangePassword();
            }
        });
        mEdtConfirmNewPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.btnSubmitChangePasswordScreen || id == EditorInfo.IME_NULL ||
                        id == EditorInfo.IME_ACTION_DONE) {
                    doChangePassword();
                    return true;
                }
                return false;
            }
        });
    }

    /***
     * Attemp change password
     */
    private void doChangePassword() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        if (checkValidation()) {
            if (!mAppUtils.getConnectionState()) {
                mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
            } else {
                mSharePref = new SharedPrefrences(getApplicationContext());

                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();
                mApiCall.doChangePassword(mSharePref.getLoginUser().getStudentID(),
                        strNewPassword, strOldPassword,
                        new OnTaskCompleted(this), mApiCall.ChangePassword);

            }
        }
    }

    /***
     * Check validation for the Change Password
     *
     * @return
     */
    private boolean checkValidation() {
        boolean status = false;

        mEdtOldPassword.setError(null);
        mEdtNewPassword.setError(null);
        mEdtConfirmNewPassword.setError(null);

        strOldPassword = mEdtOldPassword.getText().toString().trim();
        strNewPassword = mEdtNewPassword.getText().toString().trim();
        strConfirmNewPassword = mEdtConfirmNewPassword.getText().toString().trim();


        if (TextUtils.isEmpty(strOldPassword))
            mEdtOldPassword.setError(getResources().getString(R.string.error_field_required));
        if (TextUtils.isEmpty(strNewPassword))
            mEdtNewPassword.setError(getResources().getString(R.string.error_field_required));
        if (TextUtils.isEmpty(strConfirmNewPassword))
            mEdtConfirmNewPassword.setError(getResources().getString(R.string.error_field_required));

        if (strOldPassword.length() < 6) {
            mEdtOldPassword.setError(getResources().getString(R.string.error_invalid_password));
        } else if (strNewPassword.length() < 6) {
            mEdtNewPassword.setError(getResources().getString(R.string.error_invalid_password));
        } else if (!strNewPassword.equals(strConfirmNewPassword))
            mEdtConfirmNewPassword.setError("New and confirm password not match.");
        else {
            status = true;
        }
        return status;
    }


    /***
     * Parse the response of Change Password
     *
     * @param response
     */
    private void parseResponseForChangePasswordData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.ChangePassword);
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
                mEdtOldPassword.setText("");
                mEdtNewPassword.setText("");
                mEdtConfirmNewPassword.setText("");

                mSharePref.setPreferences(mSharePref.USERDETAIL,"");
                Intent intent = new Intent(getApplicationContext(), LoginScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
