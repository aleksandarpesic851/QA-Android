package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.json.JSONObject;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.DebugLog;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import buy.BuyViewPagerAdapter;
import zoho.ZohoUtils;


public class PaymentOptionScreenActivity extends AppCompatActivity
        implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private int PackageID;
    private int SUCCESS_REQUEST_CODE = 101;
    private ProgressDialog mProgressDialog;
    private AppUtil mAppUtils;
    private ApiCall mApiCall;
    private SharedPrefrences mSharePref;

    LinearLayout llPaytm, llPayPal;
    Button btnPaytm, btnPaypal;

    //Get currency type
    private String currencyType = BuyViewPagerAdapter.CURRENCY_TYPE_INR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.package_option_activity);
        getSupportActionBar().setTitle("Buy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDeclaration();

        //Hide Zoho Chat
        ZohoUtils.hideZohoChat();

    }


    /***
     * Intilize the resource
     */
    private void mDeclaration() {

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(this);
        mProgressDialog = new ProgressDialog(PaymentOptionScreenActivity.this);
        mProgressDialog.setCancelable(false);
        mSharePref = new SharedPrefrences(getApplicationContext());

        Intent intent = getIntent();
        if (intent.hasExtra("PackageID")) {
            PackageID = intent.getIntExtra("PackageID", 0);
        }

        if (intent.hasExtra(BuyViewPagerAdapter.KEY_CURRENCY_TYPE)) {
            currencyType = intent.getStringExtra(BuyViewPagerAdapter.KEY_CURRENCY_TYPE);
        }

        ((ImageView) findViewById(R.id.imgPaypalPaymentOptionScreen)).setOnClickListener(this);
        ((ImageView) findViewById(R.id.imgPaytmPaymentOptionScreen)).setOnClickListener(this);

        llPaytm = (LinearLayout) findViewById(R.id.llPaytm);
        llPayPal = (LinearLayout) findViewById(R.id.llPayPal);

        //Changes for new designs
        btnPaytm = (Button) findViewById(R.id.btnPaytm);
        btnPaypal = (Button) findViewById(R.id.btnPaypal);

        btnPaytm.setOnClickListener(this);
        btnPaypal.setOnClickListener(this);

        if (currencyType.equalsIgnoreCase(BuyViewPagerAdapter.CURRENCY_TYPE_INR)) {
            llPaytm.setVisibility(View.VISIBLE);
            llPayPal.setVisibility(View.GONE);

        } else {
            llPaytm.setVisibility(View.GONE);
            llPayPal.setVisibility(View.VISIBLE);
        }

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
    public void onClick(View v) {
        switch (v.getId()) {

           /* case R.id.imgPaypalPaymentOptionScreen:
                Intent intentPayPal = new Intent(getApplicationContext(), PaymentWebViewScreenActivity.class);
                intentPayPal.putExtra("PackageID", PackageID);
                intentPayPal.putExtra("PaymentType", "Paypal");
                startActivityForResult(intentPayPal, SUCCESS_REQUEST_CODE);
                break;

            case R.id.imgPaytmPaymentOptionScreen:
                Intent intentPaytm = new Intent(getApplicationContext(), PaymentWebViewScreenActivity.class);
                intentPaytm.putExtra("PackageID", PackageID);
                intentPaytm.putExtra("PaymentType", "Paytm");
                startActivityForResult(intentPaytm, SUCCESS_REQUEST_CODE);
                break;*/

            case R.id.btnPaytm:
                Intent intentPaytm = new Intent(getApplicationContext(), PaymentWebViewScreenActivity.class);
                intentPaytm.putExtra("PackageID", PackageID);
                intentPaytm.putExtra("PaymentType", "Paytm");
                startActivityForResult(intentPaytm, SUCCESS_REQUEST_CODE);
                break;

            case R.id.btnPaypal:
                Intent intentPayPal = new Intent(getApplicationContext(), PaymentWebViewScreenActivity.class);
                intentPayPal.putExtra("PackageID", PackageID);
                intentPayPal.putExtra("PaymentType", "Paypal");
                startActivityForResult(intentPayPal, SUCCESS_REQUEST_CODE);
                break;


            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SUCCESS_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
               /* Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();*/
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        getStudentSubscriptionEndDate();
                    }
                }, 2000L);
            }
        }
    }

    /***
     * Get Student subscription end date after Success payment
     */
    private void getStudentSubscriptionEndDate() {
        String DeviceID = mAppUtils.getDeviceID();
        LoginUser User = (LoginUser) mSharePref.getLoginUser();
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else if (DeviceID == null)
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.error_device_id_not_found));
        else {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
            DebugLog.v("Device", "Studen ID " + User.getStudentID());
            mApiCall.getUserSubscription(User.getStudentID(), DeviceID,
                    new OnTaskCompleted(this), mApiCall.GetUserSubscription);

        }
    }


    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.GetUserSubscription)) {
            parseResponseForGetSubscriptionEndDateData(result);
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
     * Parse the response of Getting Subscription end date data
     *
     * @param response
     */
    private void parseResponseForGetSubscriptionEndDateData(JSONObject response) {
        Intent intent = new Intent();
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetUserSubscription);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1) {
                Toast.makeText(this, getResources().getString(R.string.Error_Msg_Try_Later), Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_CANCELED, intent);
            } else if (ErrorCode == 2) {
                Toast.makeText(this, mJsonObj.getString("Message"), Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_CANCELED, intent);
            } else if (ErrorCode == 0) {
                mJsonObj = mJsonObj.getJSONObject("data");
                LoginUser User = mSharePref.getLoginUser();
                User.setSubscriptionPeriod(mJsonObj.getString("SubscriptionPeriod"));
                User.setSubscriptionEndDate(mJsonObj.getString("SubscriptionEndDate"));

                Gson gson = new Gson();
                String jsonString = gson.toJson(User);

                //Update User Detail
                mSharePref.setPreferences(mSharePref.USERDETAIL, jsonString);
                setResult(Activity.RESULT_OK, intent);

            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            setResult(Activity.RESULT_CANCELED, intent);
            ErrorLog.SendErrorReport(e);
        } finally {
            finish();
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
