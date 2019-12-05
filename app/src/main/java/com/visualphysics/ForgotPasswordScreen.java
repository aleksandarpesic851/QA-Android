package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;



public class ForgotPasswordScreen extends AppCompatActivity implements View.OnClickListener,OnTaskCompleted.CallBackListener {

    private EditText mEdtEmail;
    private Button mBtnSubmit;
    private String strEmail;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_screen);
        getSupportActionBar().setTitle("Forgot Password");

        mDeclaration();

    }


    /***
     * Intilize the resource
     */
    private void mDeclaration() {
        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getApplicationContext());
        mProgressDialog = new ProgressDialog(ForgotPasswordScreen.this);
        mProgressDialog.setCancelable(false);


        mEdtEmail = (EditText) findViewById(R.id.edtEmailForgotPasswordScreen);

        mBtnSubmit = (Button) findViewById(R.id.btnSubmit_ForgotPasswordScreen);
        mBtnSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit_ForgotPasswordScreen:
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (checkValidation()) {
                    if(!mAppUtils.getConnectionState()){
                        mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
                    }
                    else{
                        mProgressDialog.setMessage("Please wait...");
                        mProgressDialog.show();
                        mApiCall.doForgotPassword(strEmail, new OnTaskCompleted(this), mApiCall.ForgotPassword);
                    }
                }
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
        if(Method.equals(mApiCall.ForgotPassword)){
            parseResponseForForgotPasswordData(result);
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

    /***
     * Parse the of
     * @param response
     */
    private void parseResponseForForgotPasswordData(JSONObject response){
        try{
            JSONObject mJsonObj = response.getJSONObject(mApiCall.ForgotPassword);

            int ErrorCode = mJsonObj.getInt("Error");
            if(ErrorCode ==  1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if(ErrorCode ==  2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if(ErrorCode == 0){
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
                mEdtEmail.setText("");

            }
        }
        catch (Exception e){
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
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
        strEmail = mEdtEmail.getText().toString().trim();
        mEdtEmail.setError(null);
        if (!TextUtils.isEmpty(strEmail)){
            if(mAppUtils.isEmailValid(strEmail))
                status = true;
            else{
                mEdtEmail.setError(getResources().getString(R.string.error_invalid_email));
            }
        }
        else{
            mEdtEmail.setError(getResources().getString(R.string.error_field_required));
        }

        return status;
    }

    /***
     * Dismiss Dialog
     */
    private void dismissDialog(){
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

}
