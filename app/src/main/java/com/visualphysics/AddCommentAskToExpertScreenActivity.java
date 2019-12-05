package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;

/**
 * Created by India on 7/4/2016.
 */
public class AddCommentAskToExpertScreenActivity extends AppCompatActivity implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;

    private EditText mEdtTitle,mEdtComment;
    private String strTitle,strComment;
    private Button mBtnSubmit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asktoexpertfrag);
        getSupportActionBar().setTitle("Ask To Expert");
        mDeclaration();
    }

    private void mDeclaration(){

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(AddCommentAskToExpertScreenActivity.this);
        mProgressDialog = new ProgressDialog(AddCommentAskToExpertScreenActivity.this);
        mProgressDialog.setCancelable(false);

        mEdtTitle = (EditText) findViewById(R.id.edtTitleAddCommentAskToExpertScreen);
        mEdtComment = (EditText) findViewById(R.id.edtDescAddCommentAskToExpertScreen);

        mBtnSubmit = (Button) findViewById(R.id.btnSubmitAddCommentAskToExpertScreen);
        mBtnSubmit.setOnClickListener(this);

       /* mEdtComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.edtPasswordLoginScreen || id == EditorInfo.IME_NULL ||
                        id == EditorInfo.IME_ACTION_DONE) {
                    addComment();
                    return true;
                }
                return false;
            }
        });*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSubmitAddCommentAskToExpertScreen:
                addComment();
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
        if (Method.equals(mApiCall.AddCommentAskToExpert)) {
            parseResponseForAddCommentAskToExpertScreenData(result);
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

    private void addComment(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

        if (checkValidation()) {
            if (!mAppUtils.getConnectionState()) {
                mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
            } else {
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();
                SharedPrefrences mSharedPref = new SharedPrefrences(AddCommentAskToExpertScreenActivity.this);
                mApiCall.doAddCommentAskToExpert(mSharedPref.getLoginUser().getStudentID(),
                        strTitle,strComment,
                        new OnTaskCompleted(this), mApiCall.AddCommentAskToExpert);

            }
        }

    }

    /***
     * Check validation for the sign up
     *
     * @return
     */
    private boolean checkValidation(){
        boolean status = false;

        mEdtTitle.setError(null);
        mEdtComment.setError(null);

        strTitle = mEdtTitle.getText().toString().trim();
        strComment = mEdtComment.getText().toString().trim();

        if(!TextUtils.isEmpty(strTitle) && !TextUtils.isEmpty(strComment)) {
            status = true;
        }
        else{
            if (TextUtils.isEmpty(strTitle))
                mEdtTitle.setError(getResources().getString(R.string.error_field_required));
            if (TextUtils.isEmpty(strComment))
                mEdtComment.setError(getResources().getString(R.string.error_field_required));
        }

        return status;
    }

    /***
     * Parse the response of Add Comment data
     *
     * @param response
     */
    private void parseResponseForAddCommentAskToExpertScreenData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AddCommentAskToExpert);

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

                mEdtComment.setText("");
                mEdtTitle.setText("");
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();

            }
        } catch (Exception e) {
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
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
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
    }



}
