package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.pyze.android.PyzeEvents;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import Model.Chapters;
import Model.LoginUser;
import Utils.AppUtil;
import Utils.DebugLog;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import firebase.FirebaseClass;
import mixpanel.MixPanelClass;
import zoho.ZohoUtils;

public class PaymentWebViewScreenActivity extends AppCompatActivity implements OnTaskCompleted.CallBackListener {

    private WebView mWebview;
    private ProgressBar progressbar;
    private int PackageID;
    private String UserID;
    private SharedPrefrences mSharePref;
    private LoginUser LoginUser;
    private String URL;

    private ProgressDialog mProgressDialog;
    private AppUtil mAppUtils;
    private ApiCall mApiCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_web_view_screen_activity);
        mDecalaration();

        //Hide Zoho Chat
        ZohoUtils.hideZohoChat();

    }

    /***
     * Initialize member variable
     */
    private void mDecalaration() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(this);
        mProgressDialog = new ProgressDialog(PaymentWebViewScreenActivity.this);
        mProgressDialog.setCancelable(false);

        mSharePref = new SharedPrefrences(getApplicationContext());
        Intent intent = getIntent();
        if (intent.hasExtra("PackageID")) {
            PackageID = intent.getIntExtra("PackageID", 0);
            LoginUser = (LoginUser) mSharePref.getLoginUser();
            UserID = LoginUser.getStudentID();

            if (intent.getStringExtra("PaymentType").equals("Paypal")) {
                getSupportActionBar().setTitle("Paypal");
                URL = ApiCall.DOMAIN_ROOT_NAME + "/api/paypal/index/" + UserID + "/" + PackageID;
            } else {
                getSupportActionBar().setTitle("Paytm");
                URL = ApiCall.DOMAIN_ROOT_NAME + "/api/paytm/redirect/" + UserID + "/" + PackageID;
            }

        } else {
            finish();
            Toast.makeText(getApplicationContext(), "Please try later.", Toast.LENGTH_LONG).show();
        }
        mWebview = (WebView) findViewById(R.id.webViewPaymentScreenActivity);
        progressbar = (ProgressBar) findViewById(R.id.progressBarPaymentScreenActivity);

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
                progressbar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean shouldOverride = false;

                return shouldOverride;
            }
        });

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.addJavascriptInterface(new PaymentGatewayJavaScriptInterface(getApplicationContext()), "PaymentResponse");

        mWebview.loadUrl(URL);
        addPaymentStartEvent();

    }

    public class PaymentGatewayJavaScriptInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        PaymentGatewayJavaScriptInterface(Context c) {
            mContext = c;
        }

        /*  public void success(JSONObject mJsonObject, String paymentStatus) {

              DebugLog.v("Success","PayuMoney Success "+mJsonObject.toString());
              DebugLog.v("Success","PayuMoney Success paymentStatus "+paymentStatus);
          }*/
        @JavascriptInterface
        public void success(String Message, String paymentStatus) {
            if (paymentStatus.equals("1")) { // Transaction Success Full;
                Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_LONG).show();
                //finish();
                 /*new Handler().postDelayed(new Runnable() {
                     public void run() {
                         //getStudentSubscriptionEndDate();
                         Intent intent = new Intent();
                         setResult(Activity.RESULT_OK, intent);
                         finish();
                     }
                 }, 2000L); */

                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();

                new FirebaseClass(PaymentWebViewScreenActivity.this).sendAnalyticsData(0, "Payment Success", AppUtil.EVENT_CONVERSION);
                new FirebaseClass(PaymentWebViewScreenActivity.this).sendCustomAnalyticsData(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, "Package Purchased");

                //IZISS MPE
                sendToMixpanel(true, false);

            } else {

                addPaymentFailedEvent(Message);
                Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_LONG).show();
                finish();

                //IZISS MPE
                sendToMixpanel(false, false);
                new FirebaseClass(PaymentWebViewScreenActivity.this).sendAnalyticsData(0, "Payment Fail", AppUtil.EVENT_CONVERSION);
            }

        }

        @JavascriptInterface
        public void doIt(String a, String b) {

            JSONArray result = new JSONArray();
            result.put("Hello " + a);
            result.put("Hello " + b);

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        addPaymentAbandonedEvent();

    }

    /***
     * Get Student subscription end date after Success payment
     */
    private void getStudentSubscriptionEndDate() {
        String DeviceID = mAppUtils.getDeviceID();
        LoginUser = (LoginUser) mSharePref.getLoginUser();
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else if (DeviceID == null)
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.error_device_id_not_found));
        else {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
            DebugLog.v("Device", "Studen ID " + LoginUser.getStudentID());
            mApiCall.getUserSubscription(LoginUser.getStudentID(), DeviceID,
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


    /***
     * Add event when user cancel the payment method
     */
    private void addPaymentAbandonedEvent() {
        /*09th July 2019, Resolved Pyze library issue*/
        HashMap<String, Object> customAttributes = new HashMap<String, Object>();
        customAttributes.put("packageID", String.valueOf(PackageID));
        customAttributes.put("userID", String.valueOf(UserID));
        customAttributes.put("paymentType", getIntent().getStringExtra("PaymentType"));
        PyzeEvents.PyzeCommercePayment.postPaymentAbandoned(customAttributes);

        sendToMixpanel(false, true);

    }

    /***
     * Add event when payment fail
     */
    private void addPaymentFailedEvent(String Messsage) {

        /*09th July 2019, Resolved Pyze library issue*/
        HashMap<String, Object> customAttributes = new HashMap<String, Object>();
        customAttributes.put("packageID", String.valueOf(PackageID));
        customAttributes.put("userID", String.valueOf(UserID));
        customAttributes.put("paymentType", getIntent().getStringExtra("PaymentType"));
        customAttributes.put("failMessage", Messsage);
        PyzeEvents.PyzeCommercePayment.postPaymentFailed(customAttributes);
    }

    /***
     * Add event when payment start
     */
    private void addPaymentStartEvent() {
        /*09th July 2019, Resolved Pyze library issue*/
        HashMap<String, Object> customAttributes = new HashMap<String, Object>();
        customAttributes.put("packageID", String.valueOf(PackageID));
        customAttributes.put("userID", String.valueOf(UserID));
        customAttributes.put("paymentType", getIntent().getStringExtra("PaymentType"));
        PyzeEvents.PyzeCommercePayment.postPaymentStarted(customAttributes);
    }


    /***
     * Send data to mixpanel
     *
     * @param isPaymentSuccess
     */
    private void sendToMixpanel(boolean isPaymentSuccess, boolean isPaymentAbandoned) {
        {
            //This object will accept key and its value to send with MixPanel data
            HashMap<String, String> hashMap = new HashMap<>();

            //Key and their values which will be send to Mix Panel
            hashMap.put("PACKAGE_ID", "" + PackageID);

            //Initialize Mixpanel class and sent it
            MixPanelClass mixPanelClass = new MixPanelClass(this);

            if (isPaymentSuccess) {
                //Send data to our function which will be further sent to Mix Panel
                mixPanelClass.sendData(MixPanelClass.MPE_BOUGHT, hashMap);

                mixPanelClass.updateMixPanelPeopleAttribute(MixPanelClass.MPA_PACKAGE, "PAID WITH PKG - " + PackageID + "", false);

            } else {

                hashMap.put("DATE", AppUtil.getCurrentDateTime(this));

                if (isPaymentAbandoned) {

                    mixPanelClass.sendData(MixPanelClass.MPE_PAYMENT_ABANDONED, hashMap);
                    // Update payment failed count
                    mixPanelClass.updateMixPanelPeopleAttribute(MixPanelClass.MPA_PAYMENT_ABANDONED, "1.0", true);

                } else {

                    //Send data to our function which will be further sent to Mix Panel
                    mixPanelClass.sendData(MixPanelClass.MPE_PAYMENT_FAILED, hashMap);

                }
            }

        }
    }


}
