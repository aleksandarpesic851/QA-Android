package com.visualphysics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;

public class FreeSubscriptionScreen extends AppCompatActivity implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private TextView mTxtReferCode;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;

    private SharedPrefrences mSharedPref;
    private LoginUser User;

    private String strShareMessage = "Please refer your and earn free trial version";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_subscription_screen);
        mDeclaration();

    }

    /**
     * Intialize member variables
     */
    void mDeclaration() {

        mSharedPref = new SharedPrefrences(getApplicationContext());
        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getApplicationContext());
        mProgressDialog = new ProgressDialog(FreeSubscriptionScreen.this);
        mProgressDialog.setCancelable(false);

        mTxtReferCode = (TextView) findViewById(R.id.txtReferCodeFreeSubscriptionScreen);

        RelativeLayout mContinue = (RelativeLayout) findViewById(R.id.clickable_layoutResultScreen);
        mContinue.setOnClickListener(this);

        ((Button) findViewById(R.id.btnReferfriendFreeSubscriptionScreen)).setOnClickListener(this);

        User = mSharedPref.getLoginUser();
        if(User!=null){
            mTxtReferCode.setText(User.getReferralCode());

        }

       /* String UTCTime = mAppUtils.getUTCTime();

        TextView mTxtLocalTime = (TextView) findViewById(R.id.txtDeviceTime);
        mTxtLocalTime.setText(UTCTime);
        String LocalDateTimeString = "";
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
            Date LocalDateTime = new Date();
            LocalDateTimeString = format.format(LocalDateTime);
        }
        catch (Exception e){

        }
        mTxtLocalTime.setText("Mobile Local Date = "+LocalDateTimeString +"\n"+ "Mobile UTC Date = "+UTCTime);


        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
            mApiCall.getServerTime(new OnTaskCompleted(this), mApiCall.GetServerTime);


        }*/

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clickable_layoutResultScreen:
                finish();
                break;
            case R.id.btnReferfriendFreeSubscriptionScreen:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, strShareMessage);
                startActivity(Intent.createChooser(shareIntent, "Visual Physics"));
                break;
        }
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.GetServerTime)) {
            parseResponseForGetServerTimeData(result);
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
     * Parse the of
     *
     * @param response
     */
    private void parseResponseForGetServerTimeData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetServerTime);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {

                mJsonObj = mJsonObj.getJSONObject("data");
                String UTCTime = mAppUtils.getUTCTime();

                TextView mTxtLocalTime = (TextView) findViewById(R.id.txtDeviceTime);
                mTxtLocalTime.setText(UTCTime);
                String LocalDateTimeString = "";
                try {
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
                    Date LocalDateTime = new Date();
                    LocalDateTimeString = format.format(LocalDateTime);
                }
                catch (Exception e){
                    e.printStackTrace();
                    ErrorLog.SendErrorReport(e);
                }
                mTxtLocalTime.setText("Mobile Local Date = "+LocalDateTimeString +"\n"+ "Mobile UTC Date = "+UTCTime
                        +"\n\n"+
                        "Server Local Date = "+mJsonObj.getString("serverTime") +"\n"+
                        "Server UTC Date = "+mJsonObj.getString("UTCTime"));
            }
        } catch (Exception e) {
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
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }
}